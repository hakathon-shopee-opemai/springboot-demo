package parallax.vn.springbootdemo.adapter.in.web.order.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        String orderNumber,
        UUID customerId,
        String status,
        String currency,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal total,
        Risk risk,
        List<Line> lines,
        Instant createdAt) {

    public record Risk(Integer score, String level, String decision, List<Factor> factors) {
    }

    public record Factor(String code, int contribution) {
    }

    public record Line(String productId, String productName, BigDecimal unitPrice, int quantity,
            BigDecimal lineTotal) {
    }
}
