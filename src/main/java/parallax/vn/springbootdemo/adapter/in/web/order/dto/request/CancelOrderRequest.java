package parallax.vn.springbootdemo.adapter.in.web.order.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Request body for POST /api/v1/orders/{id}/cancel. */
public record CancelOrderRequest(@NotBlank String reason, String requestedBy) {
}
