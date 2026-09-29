package parallax.vn.springbootdemo.application.order.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Snapshot sent to the risk-service (the outbound parallax hop). */
public record OrderRiskSnapshot(
        UUID orderId,
        UUID customerId,
        BigDecimal total,
        String currency,
        String shippingCountry,
        List<LineSnapshot> lines) {

    public record LineSnapshot(String productId, int quantity, BigDecimal unitPrice) {
    }
}
