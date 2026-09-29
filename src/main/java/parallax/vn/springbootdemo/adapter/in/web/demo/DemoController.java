package parallax.vn.springbootdemo.adapter.in.web.demo;

import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import parallax.vn.springbootdemo.adapter.out.client.risk.adapter.EchoClient;
import parallax.vn.springbootdemo.adapter.out.client.risk.dto.response.PeerEchoResponse;

/** JWT-protected demo endpoints, incl. the outbound parallax hop to the risk-service. */
@RestController
@RequestMapping("/api/v1/demo")
public class DemoController {

    private final EchoClient echoClient;

    public DemoController(EchoClient echoClient) {
        this.echoClient = echoClient;
    }

    @GetMapping("/whoami")
    public Map<String, Object> whoami(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("username", jwt.getSubject(), "issuer", jwt.getIssuer().toString());
    }

    @GetMapping("/call-peer")
    public Map<String, Object> callPeer(@RequestParam(defaultValue = "hi") String message) {
        PeerEchoResponse peer = echoClient.echo(message);
        return Map.of("local", "order-service", "peer", peer);
    }
}
