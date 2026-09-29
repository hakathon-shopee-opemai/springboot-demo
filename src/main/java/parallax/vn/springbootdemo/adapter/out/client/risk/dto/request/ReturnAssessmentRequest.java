package parallax.vn.springbootdemo.adapter.out.client.risk.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

/** Wire payload for POST /internal/return-assessments (must match the risk-service schema). */
public record ReturnAssessmentRequest(
        UUID orderId,
        UUID customerId,
        BigDecimal orderTotal,
        BigDecimal requestedAmount,
        String currency,
        String reasonCode,
        String customerTier,
        int daysSinceOrder) {
}
