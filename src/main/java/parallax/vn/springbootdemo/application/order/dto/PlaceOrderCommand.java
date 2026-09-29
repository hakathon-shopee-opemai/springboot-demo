package parallax.vn.springbootdemo.application.order.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Application command to place an order (already validated at the web edge). */
public record PlaceOrderCommand(
        UUID customerId,
        String currency,
        String paymentMethod,
        Address shippingAddress,
        List<LineItem> lines,
        String idempotencyKey) {

    public record Address(String line1, String city, String country, String postalCode) {
    }

    public record LineItem(String productId, String productName, BigDecimal unitPrice, int quantity) {
    }
}
