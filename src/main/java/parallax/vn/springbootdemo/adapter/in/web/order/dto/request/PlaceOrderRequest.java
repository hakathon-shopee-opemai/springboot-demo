package parallax.vn.springbootdemo.adapter.in.web.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PlaceOrderRequest(
        @NotNull UUID customerId,
        @NotBlank String currency,
        @NotBlank String paymentMethod,
        @Valid ShippingAddress shippingAddress,
        @NotEmpty @Valid List<Line> lines,
        String idempotencyKey) {

    public record ShippingAddress(String line1, String city, String country, String postalCode) {
    }

    public record Line(
            @NotBlank String productId,
            String productName,
            @NotNull @DecimalMin("0.0") BigDecimal unitPrice,
            @Positive int quantity) {
    }
}
