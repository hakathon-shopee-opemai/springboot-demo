package parallax.vn.springbootdemo.domain.order.vo;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Immutable copy-of-record of the risk verdict stored on the order. */
public record RiskDecisionSnapshot(
        UUID assessmentId,
        Integer score,
        RiskLevel level,
        RiskDecision decision,
        List<RiskFactor> factors,
        Instant assessedAt) {
}
