package parallax.vn.springbootdemo.application.order.port.out;

import parallax.vn.springbootdemo.application.order.dto.OrderRiskSnapshot;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecisionSnapshot;

/** Outbound port to the risk-service. Implementations must fail safe (never auto-approve). */
public interface RiskAssessmentPort {
    RiskDecisionSnapshot assess(OrderRiskSnapshot snapshot);
}
