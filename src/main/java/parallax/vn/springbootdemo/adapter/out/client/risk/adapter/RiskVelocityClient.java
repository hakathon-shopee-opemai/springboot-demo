package parallax.vn.springbootdemo.adapter.out.client.risk.adapter;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.RestClient;
import parallax.vn.springbootdemo.adapter.out.client.risk.dto.request.VelocityAdjustRequest;
import parallax.vn.springbootdemo.adapter.out.client.risk.dto.response.VelocityAdjustResponse;
import parallax.vn.springbootdemo.application.order.dto.VelocityAdjustResult;
import parallax.vn.springbootdemo.application.order.port.out.RiskVelocityPort;
import parallax.vn.springbootdemo.common.annotation.PersistenceAdapter;

/** Outbound hop: adjust the customer velocity counter on the risk-service (idempotent by opId). */
@PersistenceAdapter
public class RiskVelocityClient implements RiskVelocityPort {

    private final RestClient client;

    public RiskVelocityClient(@Qualifier("riskServiceClient") RestClient client) {
        this.client = client;
    }

    @Override
    public VelocityAdjustResult adjust(UUID customerId, int delta, String operationId) {
        VelocityAdjustResponse res = client.post()
                .uri("/internal/risk/velocity/adjust")
                .body(new VelocityAdjustRequest(customerId, delta, operationId))
                .retrieve()
                .body(VelocityAdjustResponse.class);
        if (res == null) {
            throw new IllegalStateException("empty velocity adjust response");
        }
        return new VelocityAdjustResult(res.previousCount(), res.newCount(), res.applied());
    }
}
