package parallax.vn.springbootdemo.application.order.usecase;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import parallax.vn.springbootdemo.application.customer.port.out.CustomerRepositoryPort;
import parallax.vn.springbootdemo.application.order.dto.CancelOrderCommand;
import parallax.vn.springbootdemo.application.order.dto.CancellationResult;
import parallax.vn.springbootdemo.application.order.port.in.CancelOrderUseCase;
import parallax.vn.springbootdemo.application.order.port.out.DistributedLockPort;
import parallax.vn.springbootdemo.application.order.port.out.InventoryPort;
import parallax.vn.springbootdemo.application.order.port.out.OrderCancellationRepositoryPort;
import parallax.vn.springbootdemo.application.order.port.out.OrderDecisionCachePort;
import parallax.vn.springbootdemo.application.order.port.out.OrderRepositoryPort;
import parallax.vn.springbootdemo.application.order.port.out.RefundPort;
import parallax.vn.springbootdemo.application.order.port.out.RiskAssessmentCancelPort;
import parallax.vn.springbootdemo.application.order.port.out.RiskVelocityPort;
import parallax.vn.springbootdemo.common.annotation.UseCase;
import parallax.vn.springbootdemo.common.exception.code.ErrorCode;
import parallax.vn.springbootdemo.common.exception.model.ApplicationException;
import parallax.vn.springbootdemo.domain.customer.aggregate.Customer;
import parallax.vn.springbootdemo.domain.order.aggregate.Order;
import parallax.vn.springbootdemo.domain.order.aggregate.OrderCancellation;
import parallax.vn.springbootdemo.domain.order.entity.OrderLine;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;
import parallax.vn.springbootdemo.domain.order.vo.OrderStatus;
import parallax.vn.springbootdemo.domain.order.vo.SagaStep;

/**
 * Orchestrates order cancellation as a compensating saga guarded by a Redis distributed lock
 * and optimistic concurrency. Compensatable steps run forward; on any failure their
 * compensations run in reverse and the order keeps its original state (docs/05).
 */
@UseCase
public class CancelOrderSaga implements CancelOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(CancelOrderSaga.class);
    private static final Duration LOCK_TTL = Duration.ofSeconds(10);

    private final DistributedLockPort lock;
    private final OrderRepositoryPort orders;
    private final CustomerRepositoryPort customers;
    private final OrderCancellationRepositoryPort cancellations;
    private final InventoryPort inventory;
    private final RiskVelocityPort velocity;
    private final RiskAssessmentCancelPort assessmentCancel;
    private final RefundPort refund;
    private final OrderDecisionCachePort cache;
    private final CancellationFinalizer finalizer;

    public CancelOrderSaga(DistributedLockPort lock, OrderRepositoryPort orders,
            CustomerRepositoryPort customers, OrderCancellationRepositoryPort cancellations,
            InventoryPort inventory, RiskVelocityPort velocity,
            RiskAssessmentCancelPort assessmentCancel, RefundPort refund,
            OrderDecisionCachePort cache, CancellationFinalizer finalizer) {
        this.lock = lock;
        this.orders = orders;
        this.customers = customers;
        this.cancellations = cancellations;
        this.inventory = inventory;
        this.velocity = velocity;
        this.assessmentCancel = assessmentCancel;
        this.refund = refund;
        this.cache = cache;
        this.finalizer = finalizer;
    }

    @Override
    public CancellationResult cancelOrder(CancelOrderCommand command) {
        OrderId orderId = OrderId.of(command.orderId());
        String lockKey = "lock:order:" + orderId.value();
        String token = lock.tryLock(lockKey, LOCK_TTL)
                .orElseThrow(() -> new ApplicationException(ErrorCode.CONFLICT,
                        "order is being modified, try again"));
        try {
            // idempotency (by key, then by existing saga record)
            Optional<CancellationResult> replay = replayIfDone(orderId, command.idempotencyKey());
            if (replay.isPresent()) {
                return replay.get();
            }

            Order order = orders.findById(orderId)
                    .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND, "order not found"));
            if (!order.status().canTransitionTo(OrderStatus.CANCELLED)) {
                throw new ApplicationException(ErrorCode.BUSINESS_RULE,
                        "order not cancellable in status " + order.status());
            }
            Customer customer = customers.findById(order.customerId())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.INTERNAL,
                            "customer missing for order"));

            OrderCancellation saga = cancellations.save(OrderCancellation.start(orderId,
                    command.reason(), command.requestedBy(), command.idempotencyKey()));

            Deque<Runnable> compensations = new ArrayDeque<>();
            try {
                // S1 release inventory  (comp: re-reserve)
                inventory.release(orderId);
                compensations.push(() -> reReserve(orderId, order));
                saga.recordStep(SagaStep.done("releaseInventory"));
                saga = cancellations.save(saga);

                // S2 velocity -1 on risk-service  (comp: velocity +1)
                velocity.adjust(order.customerId().value(), -1, orderId.value() + ":cancel");
                compensations.push(() ->
                        velocity.adjust(order.customerId().value(), 1, orderId.value() + ":comp"));
                saga.recordStep(SagaStep.done("velocityAdjust"));
                saga = cancellations.save(saga);

                // S3 refund  (comp: reverse refund)
                refund.refund(orderId);
                compensations.push(() -> refund.reverseRefund(orderId));
                saga.recordStep(SagaStep.done("refund"));
                saga = cancellations.save(saga);

                // FINALIZE (single tx): order -> CANCELLED, reverse customer stats, saga COMPLETED
                order.cancel();
                customer.reverseOrder(order.total());
                saga = finalizer.finalizeSuccess(order, customer, saga);

                // best-effort side effects (not compensated)
                assessmentCancel.cancel(orderId.value(), command.reason());
                cache.evictDecision(orderId);
                cache.evictCustomerHistory(order.customerId());
                if (hasText(command.idempotencyKey())) {
                    cache.rememberCancelIdempotencyKey(command.idempotencyKey(), orderId);
                }
                log.info("order cancelled order={} saga=COMPLETED", orderId.value());
                return toResult(order.status().name(), saga);
            } catch (OptimisticLockingFailureException ole) {
                compensate(compensations);
                markCompensated(saga);
                throw new ApplicationException(ErrorCode.CONFLICT,
                        "order changed concurrently; cancellation rolled back", ole);
            } catch (ApplicationException ae) {
                compensate(compensations);
                markCompensated(saga);
                throw ae;
            } catch (Exception ex) {
                compensate(compensations);
                markCompensated(saga);
                throw new ApplicationException(ErrorCode.UPSTREAM_UNAVAILABLE,
                        "cancellation failed and was compensated: " + ex.getMessage(), ex);
            }
        } finally {
            lock.unlock(lockKey, token);
        }
    }

    private Optional<CancellationResult> replayIfDone(OrderId orderId, String idempotencyKey) {
        Optional<OrderCancellation> existing = cancellations.findByOrderId(orderId);
        if (existing.isPresent()) {
            OrderCancellation c = existing.get();
            if (c.isCompleted()) {
                String status = orders.findById(orderId)
                        .map(o -> o.status().name()).orElse(OrderStatus.CANCELLED.name());
                return Optional.of(toResult(status, c));
            }
            if (c.isInProgress()) {
                throw new ApplicationException(ErrorCode.CONFLICT,
                        "a cancellation is already in progress for this order");
            }
        }
        return Optional.empty();
    }

    private void reReserve(OrderId orderId, Order order) {
        for (OrderLine line : order.lines()) {
            inventory.reserveLine(orderId, line);
        }
    }

    private void compensate(Deque<Runnable> compensations) {
        while (!compensations.isEmpty()) {
            try {
                compensations.pop().run();
            } catch (Exception ce) {
                log.error("compensation step failed: {}", ce.getMessage(), ce);
            }
        }
    }

    private void markCompensated(OrderCancellation saga) {
        try {
            saga.compensated();
            cancellations.save(saga);
        } catch (Exception e) {
            log.error("failed to persist COMPENSATED saga state: {}", e.getMessage(), e);
        }
    }

    private CancellationResult toResult(String orderStatus, OrderCancellation saga) {
        List<CancellationResult.Step> steps = saga.steps().stream()
                .map(s -> new CancellationResult.Step(s.name(), s.status()))
                .toList();
        return new CancellationResult(saga.orderId().value(), orderStatus, saga.status().name(),
                saga.reason(), steps);
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
