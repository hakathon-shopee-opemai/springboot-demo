package parallax.vn.springbootdemo.adapter.out.client.risk.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Wire payload returned by the risk-service for a return assessment. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReturnAssessmentResponse(
        String assessmentId,
        String orderId,
        Integer score,
        String level,
        String decision,
        List<Factor> factors,
        String assessedAt) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Factor(String code, String description, Integer weight, Integer contribution) {
    }
}
