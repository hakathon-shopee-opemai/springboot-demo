package parallax.vn.springbootdemo.application.order.port.out;

import java.util.UUID;

/** Outbound port: annotate the stored assessment as cancelled (best-effort). */
public interface RiskAssessmentCancelPort {
    void cancel(UUID orderId, String reason);
}
