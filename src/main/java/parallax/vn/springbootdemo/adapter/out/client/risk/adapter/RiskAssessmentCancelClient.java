package parallax.vn.springbootdemo.adapter.out.client.risk.adapter;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.RestClient;
import parallax.vn.springbootdemo.adapter.out.client.risk.dto.request.AssessmentCancelRequest;
import parallax.vn.springbootdemo.application.order.port.out.RiskAssessmentCancelPort;
import parallax.vn.springbootdemo.common.annotation.PersistenceAdapter;

/** Best-effort annotation of the risk assessment as cancelled (not compensated). */
@PersistenceAdapter
public class RiskAssessmentCancelClient implements RiskAssessmentCancelPort {

    private static final Logger log = LoggerFactory.getLogger(RiskAssessmentCancelClient.class);

    private final RestClient client;

    public RiskAssessmentCancelClient(@Qualifier("riskServiceClient") RestClient client) {
        this.client = client;
    }

    @Override
    public void cancel(UUID orderId, String reason) {
        try {
            client.post()
                    .uri("/internal/risk-assessments/{orderId}/cancel", orderId)
                    .body(new AssessmentCancelRequest(reason))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("best-effort assessment cancel failed for {}: {}", orderId, e.getMessage());
        }
    }
}
