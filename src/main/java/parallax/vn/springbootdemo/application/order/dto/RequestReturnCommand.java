package parallax.vn.springbootdemo.application.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** Command to request a return (already validated at the web edge). */
public record RequestReturnCommand(
        UUID orderId,
        String reasonCode,
        String reason,
        BigDecimal requestedAmount,
        String idempotencyKey) {
}
