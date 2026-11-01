package parallax.vn.springbootdemo.application.order.usecase;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import parallax.vn.springbootdemo.application.customer.port.out.CustomerRepositoryPort;
import parallax.vn.springbootdemo.application.order.dto.ReturnAssessmentSnapshot;
import parallax.vn.springbootdemo.application.order.port.in.ResolveStaleReturnReviewsUseCase;
import parallax.vn.springbootdemo.application.order.port.out.OrderDecisionCachePort;
import parallax.vn.springbootdemo.application.order.port.out.OrderRepositoryPort;
import parallax.vn.springbootdemo.application.order.port.out.ReturnRepositoryPort;
import parallax.vn.springbootdemo.application.order.port.out.ReturnRiskAssessmentPort;
import parallax.vn.springbootdemo.common.annotation.UseCase;
import parallax.vn.springbootdemo.domain.customer.aggregate.Customer;
import parallax.vn.springbootdemo.domain.order.aggregate.Order;
import parallax.vn.springbootdemo.domain.order.aggregate.OrderReturn;
import parallax.vn.springbootdemo.domain.order.vo.ReturnDecision;
import parallax.vn.springbootdemo.domain.order.vo.ReturnRiskAssessment;

/**
 * Sweeps returns that sat in UNDER_REVIEW longer than the SLA: each one is re-scored on the
 * risk-service (the parallax hop again) and closed when the verdict is no longer REVIEW.
 */
@UseCase
public class ResolveStaleReturnReviewsService implements ResolveStaleReturnReviewsUseCase {

    private static final Logger log = LoggerFactory.getLogger(ResolveStaleReturnReviewsService.class);

    private final ReturnRepositoryPort returns;
    private final OrderRepositoryPort orders;
    private final CustomerRepositoryPort customers;
    private final ReturnRiskAssessmentPort riskAssessment;
    private final OrderDecisionCachePort cache;
    private final Duration reviewSla;

    public ResolveStaleReturnReviewsService(ReturnRepositoryPort returns, OrderRepositoryPort orders,
            CustomerRepositoryPort customers, ReturnRiskAssessmentPort riskAssessment,
            OrderDecisionCachePort cache,
            @Value("${returns.review-sla-hours:24}") long reviewSlaHours) {
        this.returns = returns;
        this.orders = orders;
        this.customers = customers;
        this.riskAssessment = riskAssessment;
        this.cache = cache;
        this.reviewSla = Duration.ofHours(reviewSlaHours);
    }

    @Override
    @Transactional
    public int resolveStaleReviews() {
        Instant cutoff = Instant.now().minus(reviewSla);
        int resolved = 0;
        for (OrderReturn stale : returns.findUnderReviewBefore(cutoff)) {
            Optional<Order> order = orders.findById(stale.orderId());
            Optional<Customer> customer = customers.findById(stale.customerId());
            if (order.isEmpty() || customer.isEmpty()) {
                log.warn("skip stale review {}: order/customer missing", stale.id());
                continue;
            }
            ReturnRiskAssessment verdict = riskAssessment.assess(
                    toSnapshot(order.get(), customer.get(), stale));
            if (verdict.decision() == ReturnDecision.REVIEW) {
                continue;
            }
            stale.resolveReview(verdict.decision(), verdict.score());
            returns.save(stale);
            cache.evictCustomerHistory(stale.customerId());
            resolved++;
        }
        log.info("stale return reviews resolved={} cutoff={}", resolved, cutoff);
        return resolved;
    }

    private ReturnAssessmentSnapshot toSnapshot(Order order, Customer customer, OrderReturn r) {
        int daysSinceOrder = (int) Math.max(0,
                Duration.between(order.createdAt(), Instant.now()).toDays());
        String tier = customer.isBlocked() ? "BLOCKED" : customer.tier().name();
        return new ReturnAssessmentSnapshot(order.id().value(), order.customerId().value(),
                order.total().amount(), r.requestedAmount().amount(), order.currency(),
                r.reasonCode().name(), tier, daysSinceOrder);
    }
}
