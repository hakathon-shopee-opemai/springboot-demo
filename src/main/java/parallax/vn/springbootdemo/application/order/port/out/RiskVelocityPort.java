package parallax.vn.springbootdemo.application.order.port.out;

import java.util.UUID;
import parallax.vn.springbootdemo.application.order.dto.VelocityAdjustResult;

/** Outbound port: adjust a customer's real-time velocity counter on the risk-service. */
public interface RiskVelocityPort {
    VelocityAdjustResult adjust(UUID customerId, int delta, String operationId);
}
