package parallax.vn.springbootdemo.adapter.in.web.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Request body for POST /api/v1/orders/{orderId}/returns. requestedAmount is optional (defaults to
 * the order total); when present it must be positive. */
public record CreateReturnRequest(
        @NotBlank String reasonCode,
        @Size(max = 512) String reason,
        @Positive BigDecimal requestedAmount) {
}
