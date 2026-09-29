package parallax.vn.springbootdemo.config.client;

import java.io.IOException;
import java.util.UUID;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import parallax.vn.springbootdemo.common.logging.mdc.CorrelationContext;

/** Propagates the current trace id onto outbound requests as X-Trace-Id. */
@Component
public class TracePropagationInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
            ClientHttpRequestExecution execution) throws IOException {
        String traceId = CorrelationContext.getTraceId();
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }
        request.getHeaders().add(CorrelationContext.HEADER, traceId);
        return execution.execute(request, body);
    }
}
