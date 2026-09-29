package parallax.vn.springbootdemo.domain.order.vo;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Immutable copy of the return-fraud verdict returned by the risk-service. */
public record ReturnRiskAssessment(
        UUID assessmentId,
        int score,
        RiskLevel level,
        ReturnDecision decision,
        List<RiskFactor> factors,
        Instant assessedAt) {
}
