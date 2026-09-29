package parallax.vn.springbootdemo.adapter.out.client.risk.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PeerEchoResponse(String service, String echo, String traceId, String receivedAt) {
}
