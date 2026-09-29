package parallax.vn.springbootdemo.application.order.port.out;

import java.util.Optional;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecisionSnapshot;

public interface OrderDecisionCachePort {
    Optional<RiskDecisionSnapshot> getDecision(OrderId id);

    void putDecision(OrderId id, RiskDecisionSnapshot decision);

    Optional<OrderId> resolveIdempotencyKey(String key);

    void rememberIdempotencyKey(String key, OrderId id);

    void evictDecision(OrderId id);

    void evictCustomerHistory(parallax.vn.springbootdemo.domain.customer.vo.CustomerId customerId);

    java.util.Optional<OrderId> resolveCancelIdempotencyKey(String key);

    void rememberCancelIdempotencyKey(String key, OrderId id);

    java.util.Optional<java.util.UUID> resolveReturnIdempotencyKey(String key);

    void rememberReturnIdempotencyKey(String key, java.util.UUID returnId);

    /** Increment the customer's rolling return-velocity counter (soft pre-signal). */
    void incrementReturnVelocity(parallax.vn.springbootdemo.domain.customer.vo.CustomerId customerId);
}
