package parallax.vn.springbootdemo.adapter.out.client.risk.adapter;

import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.RestClient;
import parallax.vn.springbootdemo.adapter.out.client.risk.dto.request.ReturnAssessmentRequest;
import parallax.vn.springbootdemo.adapter.out.client.risk.dto.response.ReturnAssessmentResponse;
import parallax.vn.springbootdemo.adapter.out.client.risk.mapper.ReturnAssessmentMapper;
import parallax.vn.springbootdemo.application.order.dto.ReturnAssessmentSnapshot;
import parallax.vn.springbootdemo.application.order.port.out.ReturnRiskAssessmentPort;
import parallax.vn.springbootdemo.common.annotation.PersistenceAdapter;
import parallax.vn.springbootdemo.domain.order.vo.ReturnDecision;
import parallax.vn.springbootdemo.domain.order.vo.ReturnRiskAssessment;
import parallax.vn.springbootdemo.domain.order.vo.RiskFactor;
import parallax.vn.springbootdemo.domain.order.vo.RiskLevel;

/** Outbound parallax hop to the risk-service for return-fraud scoring. Fails safe: on any error
 * it returns a conservative REVIEW verdict (never AUTO_REFUND). */
@PersistenceAdapter
public class ReturnRiskAssessmentClient implements ReturnRiskAssessmentPort {

    private static final Logger log = LoggerFactory.getLogger(ReturnRiskAssessmentClient.class);

    private final RestClient client;
    private final ReturnAssessmentMapper mapper;

    public ReturnRiskAssessmentClient(@Qualifier("riskServiceClient") RestClient client,
            ReturnAssessmentMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public ReturnRiskAssessment assess(ReturnAssessmentSnapshot snapshot) {
        try {
            ReturnAssessmentResponse response = client.post()
                    .uri("/internal/return-assessments")
                    .body(toRequest(snapshot))
                    .retrieve()
                    .body(ReturnAssessmentResponse.class);
            if (response == null) {
                return fallback();
            }
            return mapper.toDomain(response);
        } catch (Exception e) {
            log.warn("return-assessment call failed, applying fail-safe REVIEW: {}", e.getMessage());
            return fallback();
        }
    }

    private ReturnAssessmentRequest toRequest(ReturnAssessmentSnapshot s) {
        return new ReturnAssessmentRequest(s.orderId(), s.customerId(), s.orderTotal(),
                s.requestedAmount(), s.currency(), s.reasonCode(), s.customerTier(), s.daysSinceOrder());
    }

    /** Fail-safe verdict: hold for manual review, never auto-refund. */
    private ReturnRiskAssessment fallback() {
        return new ReturnRiskAssessment(null, 50, RiskLevel.MEDIUM, ReturnDecision.REVIEW,
                List.of(new RiskFactor("RISK_ENGINE_UNAVAILABLE",
                        "Risk engine unavailable; held for manual review", 0, 0)),
                Instant.now());
    }
}
