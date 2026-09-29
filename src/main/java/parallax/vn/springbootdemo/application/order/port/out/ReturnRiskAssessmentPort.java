package parallax.vn.springbootdemo.application.order.port.out;

import parallax.vn.springbootdemo.application.order.dto.ReturnAssessmentSnapshot;
import parallax.vn.springbootdemo.domain.order.vo.ReturnRiskAssessment;

/** Outbound port to the risk-service for return-fraud scoring. Implementations must fail safe
 * (never AUTO_REFUND on error — fall back to REVIEW). */
public interface ReturnRiskAssessmentPort {
    ReturnRiskAssessment assess(ReturnAssessmentSnapshot snapshot);
}
