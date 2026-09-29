package parallax.vn.springbootdemo.domain.order.vo;

import java.time.Instant;
import java.util.UUID;
import parallax.vn.springbootdemo.domain.shared.vo.Money;

/** Immutable refund ledger entry; exactly one per approved (REFUNDED) return. */
public record RefundLedger(UUID id, UUID returnId, UUID orderId, Money amount, Instant createdAt) {

    public static RefundLedger create(UUID returnId, UUID orderId, Money amount) {
        return new RefundLedger(UUID.randomUUID(), returnId, orderId, amount, Instant.now());
    }
}
