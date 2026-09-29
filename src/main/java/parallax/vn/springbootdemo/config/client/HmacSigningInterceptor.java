package parallax.vn.springbootdemo.config.client;

import java.io.IOException;
import java.time.Instant;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import parallax.vn.springbootdemo.common.util.HmacSigner;
import parallax.vn.springbootdemo.config.properties.SecurityProperties;

/** Signs outbound service-to-service requests with the shared HMAC secret. */
@Component
public class HmacSigningInterceptor implements ClientHttpRequestInterceptor {

    private final HmacSigner signer;
    private final SecurityProperties properties;

    public HmacSigningInterceptor(HmacSigner signer, SecurityProperties properties) {
        this.signer = signer;
        this.properties = properties;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
            ClientHttpRequestExecution execution) throws IOException {
        String ts = String.valueOf(Instant.now().getEpochSecond());
        String path = request.getURI().getRawPath();
        String canonical = signer.canonical(request.getMethod().name(), path, ts, body);
        String signature = signer.sign(properties.serviceSharedSecret(), canonical);
        request.getHeaders().add("X-Signature", signature);
        request.getHeaders().add("X-Signature-Ts", ts);
        return execution.execute(request, body);
    }
}
