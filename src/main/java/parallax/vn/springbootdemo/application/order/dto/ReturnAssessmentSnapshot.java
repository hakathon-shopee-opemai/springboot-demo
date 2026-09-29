package parallax.vn.springbootdemo.application.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** Snapshot sent to the risk-service for a return-fraud assessment (outbound parallax hop). */
public record ReturnAssessmentSnapshot(
        UUID orderId,
        UUID customerId,
        BigDecimal orderTotal,
        BigDecimal requestedAmount,
        String currency,
        String reasonCode,
        String customerTier,
        int daysSinceOrder) {
}
