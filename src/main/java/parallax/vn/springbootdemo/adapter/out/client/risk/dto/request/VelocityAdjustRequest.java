package parallax.vn.springbootdemo.adapter.out.client.risk.dto.request;

import java.util.UUID;

/** Wire payload for POST /internal/risk/velocity/adjust (must match risk-service). */
public record VelocityAdjustRequest(UUID customerId, int delta, String operationId) {
}
