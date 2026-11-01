package parallax.vn.springbootdemo.application.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Outcome of a return request. {@code refund} is present only when status is REFUNDED. */
public record ReturnResult(
        UUID returnId,
        UUID orderId,
        String status,
        String decision,
        int riskScore,
        RefundInfo refund,
        String reasonCode,
        Instant createdAt) {

    public record RefundInfo(BigDecimal amount, String currency, UUID ledgerId) {
    }
}
