package parallax.vn.springbootdemo.adapter.out.client.risk.adapter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import parallax.vn.springbootdemo.adapter.out.client.risk.dto.request.PeerEchoRequest;
import parallax.vn.springbootdemo.adapter.out.client.risk.dto.response.PeerEchoResponse;

/** Calls the risk-service internal echo endpoint via the signed RestClient. */
@Component
public class EchoClient {

    private final RestClient client;

    public EchoClient(@Qualifier("riskServiceClient") RestClient client) {
        this.client = client;
    }

    public PeerEchoResponse echo(String message) {
        return client.post()
                .uri("/internal/echo")
                .body(new PeerEchoRequest(message))
                .retrieve()
                .body(PeerEchoResponse.class);
    }
}
