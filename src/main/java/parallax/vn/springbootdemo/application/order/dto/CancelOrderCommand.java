package parallax.vn.springbootdemo.application.order.dto;

import java.util.UUID;

/** Command to cancel an order via the compensating saga. */
public record CancelOrderCommand(UUID orderId, String reason, String requestedBy,
        String idempotencyKey) {
}
