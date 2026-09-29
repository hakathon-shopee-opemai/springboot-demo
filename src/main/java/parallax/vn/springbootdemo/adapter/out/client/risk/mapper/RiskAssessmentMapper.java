package parallax.vn.springbootdemo.adapter.out.client.risk.mapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import parallax.vn.springbootdemo.adapter.out.client.risk.dto.response.RiskAssessmentResponse;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecision;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecisionSnapshot;
import parallax.vn.springbootdemo.domain.order.vo.RiskFactor;
import parallax.vn.springbootdemo.domain.order.vo.RiskLevel;

@Component
public class RiskAssessmentMapper {

    public RiskDecisionSnapshot toDomain(RiskAssessmentResponse r) {
        List<RiskFactor> factors = new ArrayList<>();
        if (r.factors() != null) {
            for (RiskAssessmentResponse.Factor f : r.factors()) {
                factors.add(new RiskFactor(f.code(), f.description(),
                        f.weight() == null ? 0 : f.weight(),
                        f.contribution() == null ? 0 : f.contribution()));
            }
        }
        UUID assessmentId = r.assessmentId() == null ? null : UUID.fromString(r.assessmentId());
        Instant assessedAt = r.assessedAt() == null ? Instant.now() : Instant.parse(r.assessedAt());
        return new RiskDecisionSnapshot(assessmentId, r.score(),
                RiskLevel.valueOf(r.level()), RiskDecision.valueOf(r.decision()), factors, assessedAt);
    }
}
