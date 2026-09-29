package parallax.vn.springbootdemo.adapter.out.client.risk.dto.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Wire payload for POST /internal/risk-assessments (must match the risk-service schema). */
public record RiskAssessmentRequest(
        UUID orderId,
        UUID customerId,
        BigDecimal total,
        String currency,
        String shippingCountry,
        List<Line> lines) {

    public record Line(String productId, int quantity, BigDecimal unitPrice) {
    }
}
