package parallax.vn.springbootdemo.adapter.out.client.risk.adapter;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import parallax.vn.springbootdemo.adapter.out.client.risk.dto.request.RiskAssessmentRequest;
import parallax.vn.springbootdemo.adapter.out.client.risk.dto.response.RiskAssessmentResponse;
import parallax.vn.springbootdemo.adapter.out.client.risk.mapper.RiskAssessmentMapper;
import parallax.vn.springbootdemo.application.order.dto.OrderRiskSnapshot;
import parallax.vn.springbootdemo.application.order.port.out.RiskAssessmentPort;
import parallax.vn.springbootdemo.common.annotation.PersistenceAdapter;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecision;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecisionSnapshot;
import parallax.vn.springbootdemo.domain.order.vo.RiskFactor;
import parallax.vn.springbootdemo.domain.order.vo.RiskLevel;

/** Outbound parallax hop to the risk-service. Retries transient failures, caps in-flight calls,
 * and fails safe: never auto-approves on error. */
@PersistenceAdapter
public class RiskAssessmentClient implements RiskAssessmentPort {

    private static final Logger log = LoggerFactory.getLogger(RiskAssessmentClient.class);
    private static final int MAX_ATTEMPTS = 3;
    private static final Duration BACKOFF = Duration.ofMillis(150);

    private final RestClient client;
    private final RiskAssessmentMapper mapper;

    public RiskAssessmentClient(@Qualifier("riskServiceClient") RestClient client,
            RiskAssessmentMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    @ConcurrencyLimit(8)
    public RiskDecisionSnapshot assess(OrderRiskSnapshot snapshot) {
        RiskAssessmentRequest request = toRequest(snapshot);
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                RiskAssessmentResponse response = client.post()
                        .uri("/internal/risk-assessments")
                        .body(request)
                        .retrieve()
                        .body(RiskAssessmentResponse.class);
                if (response == null) {
                    return fallback();
                }
                return mapper.toDomain(response);
            } catch (RestClientResponseException e) {
                // 4xx is a contract problem — retrying will not help
                log.warn("risk-service rejected assessment ({}), applying fail-safe REVIEW",
                        e.getStatusCode());
                return fallback();
            } catch (RestClientException e) {
                log.warn("risk-service call failed (attempt {}/{}): {}", attempt, MAX_ATTEMPTS,
                        e.getMessage());
                backoff(attempt);
            }
        }
        return fallback();
    }

    private static void backoff(int attempt) {
        try {
            Thread.sleep(BACKOFF.toMillis() * attempt);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private RiskAssessmentRequest toRequest(OrderRiskSnapshot s) {
        List<RiskAssessmentRequest.Line> lines = s.lines().stream()
                .map(l -> new RiskAssessmentRequest.Line(l.productId(), l.quantity(), l.unitPrice()))
                .toList();
        return new RiskAssessmentRequest(s.orderId(), s.customerId(), s.total(), s.currency(),
                s.shippingCountry(), lines);
    }

    /** Fail-safe verdict: hold for review, never auto-approve. */
    private RiskDecisionSnapshot fallback() {
        return new RiskDecisionSnapshot(null, 50, RiskLevel.MEDIUM, RiskDecision.REVIEW,
                List.of(new RiskFactor("RISK_ENGINE_UNAVAILABLE",
                        "Risk engine unavailable; held for manual review", 0, 0)),
                Instant.now());
    }
}
