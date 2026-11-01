package parallax.vn.springbootdemo.application.order.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import parallax.vn.springbootdemo.application.customer.port.out.CustomerRepositoryPort;
import parallax.vn.springbootdemo.application.order.dto.RequestReturnCommand;
import parallax.vn.springbootdemo.application.order.dto.ReturnAssessmentSnapshot;
import parallax.vn.springbootdemo.application.order.dto.ReturnResult;
import parallax.vn.springbootdemo.application.order.port.in.RequestReturnUseCase;
import parallax.vn.springbootdemo.application.order.port.out.OrderDecisionCachePort;
import parallax.vn.springbootdemo.application.order.port.out.OrderRepositoryPort;
import parallax.vn.springbootdemo.application.order.port.out.ReturnRepositoryPort;
import parallax.vn.springbootdemo.application.order.port.out.ReturnRiskAssessmentPort;
import parallax.vn.springbootdemo.common.annotation.RateLimited;
import parallax.vn.springbootdemo.common.annotation.UseCase;
import parallax.vn.springbootdemo.common.exception.code.ErrorCode;
import parallax.vn.springbootdemo.common.exception.model.ApplicationException;
import parallax.vn.springbootdemo.domain.customer.aggregate.Customer;
import parallax.vn.springbootdemo.domain.order.aggregate.Order;
import parallax.vn.springbootdemo.domain.order.aggregate.OrderReturn;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;
import parallax.vn.springbootdemo.domain.order.vo.RefundLedger;
import parallax.vn.springbootdemo.domain.order.vo.ReturnReason;
import parallax.vn.springbootdemo.domain.order.vo.ReturnRiskAssessment;
import parallax.vn.springbootdemo.domain.shared.vo.Money;

/**
 * Handles a return request end to end: idempotency, invariants, the fail-safe risk hop, decision
 * mapping (AUTO_REFUND→REFUNDED+ledger, REVIEW→UNDER_REVIEW, DENY→DENIED) and persistence.
 * A single DB transaction is acceptable because the risk hop is fail-safe and fast.
 */
@UseCase
public class RequestReturnService implements RequestReturnUseCase {

    private static final Logger log = LoggerFactory.getLogger(RequestReturnService.class);

    private final OrderRepositoryPort orders;
    private final ReturnRepositoryPort returns;
    private final CustomerRepositoryPort customers;
    private final ReturnRiskAssessmentPort riskAssessment;
    private final OrderDecisionCachePort cache;
    private final int returnWindowDays;

    public RequestReturnService(OrderRepositoryPort orders, ReturnRepositoryPort returns,
            CustomerRepositoryPort customers, ReturnRiskAssessmentPort riskAssessment,
            OrderDecisionCachePort cache,
            @Value("${returns.window-days:30}") int returnWindowDays) {
        this.orders = orders;
        this.returns = returns;
        this.customers = customers;
        this.riskAssessment = riskAssessment;
        this.cache = cache;
        this.returnWindowDays = returnWindowDays;
    }

    @Override
    @Transactional
    @RateLimited(key = "returns.request", permits = 10, windowSeconds = 60)
    public ReturnResult requestReturn(RequestReturnCommand command) {
        // 1. idempotency short-circuit (fast path via Redis)
        if (hasText(command.idempotencyKey())) {
            Optional<java.util.UUID> existing = cache.resolveReturnIdempotencyKey(command.idempotencyKey());
            if (existing.isPresent()) {
                return toResult(returns.findById(existing.get())
                        .orElseThrow(() -> new ApplicationException(ErrorCode.INTERNAL,
                                "idempotent return missing")));
            }
        }

        // 2. load order and guard against a double return
        OrderId orderId = OrderId.of(command.orderId());
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND, "order not found"));
        if (returns.existsActiveReturn(orderId)) {
            throw new ApplicationException(ErrorCode.CONFLICT,
                    "an active return already exists for this order");
        }
        Customer customer = customers.findById(order.customerId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.INTERNAL, "customer missing for order"));

        // 3. build the return aggregate (domain enforces state/window/amount invariants)
        ReturnReason reasonCode = parseReason(command.reasonCode());
        Money requestedAmount = command.requestedAmount() == null ? order.total()
                : Money.of(command.requestedAmount(), order.currency());
        OrderReturn orderReturn = OrderReturn.request(order, reasonCode, command.reason(),
                requestedAmount, returnWindowDays, command.idempotencyKey());

        // 4. parallax hop: return-fraud assessment (fail-safe inside the adapter)
        ReturnRiskAssessment assessment = riskAssessment.assess(
                toSnapshot(order, customer, requestedAmount, reasonCode));

        // 5. apply verdict + persist (mints the refund ledger when REFUNDED)
        orderReturn.applyDecision(assessment.decision(), assessment.score());
        orderReturn = returns.save(orderReturn);

        // 6. redis: bump velocity, remember idempotency key
        cache.incrementReturnVelocity(order.customerId());
        if (hasText(command.idempotencyKey())) {
            cache.rememberReturnIdempotencyKey(command.idempotencyKey(), orderReturn.id());
        }

        log.info("return requested order={} status={} decision={} score={}", orderId.value(),
                orderReturn.status(), orderReturn.decision(), orderReturn.riskScore());
        return toResult(orderReturn);
    }

    private ReturnAssessmentSnapshot toSnapshot(Order order, Customer customer, Money requestedAmount,
            ReturnReason reasonCode) {
        int daysSinceOrder = (int) Duration.between(order.createdAt(), Instant.now()).toDays();
        if (daysSinceOrder < 0) {
            daysSinceOrder = 0;
        }
        String tier = customer.isBlocked() ? "BLOCKED" : customer.tier().name();
        return new ReturnAssessmentSnapshot(order.id().value(), order.customerId().value(),
                order.total().amount(), requestedAmount.amount(), order.currency(),
                reasonCode.name(), tier, daysSinceOrder);
    }

    private ReturnReason parseReason(String reasonCode) {
        try {
            return ReturnReason.valueOf(reasonCode);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "unknown reasonCode: " + reasonCode);
        }
    }

    private ReturnResult toResult(OrderReturn r) {
        ReturnResult.RefundInfo refund = null;
        RefundLedger ledger = r.refund();
        if (ledger != null) {
            refund = new ReturnResult.RefundInfo(ledger.amount().amount(), ledger.amount().currency(),
                    ledger.id());
        }
        return new ReturnResult(r.id(), r.orderId().value(), r.status().name(),
                r.decision() == null ? null : r.decision().name(), r.riskScore(), refund,
                r.reasonCode().name(), r.createdAt());
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
