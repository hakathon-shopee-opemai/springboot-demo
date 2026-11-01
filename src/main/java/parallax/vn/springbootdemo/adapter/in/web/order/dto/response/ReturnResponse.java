package parallax.vn.springbootdemo.adapter.in.web.order.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Response body for a return request (contract §3). {@code refund} is present only when REFUNDED. */
public record ReturnResponse(
        UUID returnId,
        UUID orderId,
        String status,
        String decision,
        Integer riskScore,
        Refund refund,
        String reasonCode,
        Instant createdAt) {

    public record Refund(BigDecimal amount, String currency, UUID ledgerId) {
    }
}
