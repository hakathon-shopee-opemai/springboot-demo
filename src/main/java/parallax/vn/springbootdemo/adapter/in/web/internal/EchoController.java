package parallax.vn.springbootdemo.adapter.in.web.internal;

import java.time.Instant;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import parallax.vn.springbootdemo.common.logging.mdc.CorrelationContext;

/** Internal (HMAC-authenticated) echo endpoint used to exercise the parallax hop. */
@RestController
@RequestMapping("/internal")
public class EchoController {

    @PostMapping("/echo")
    public EchoResponse echo(@RequestBody EchoRequest request) {
        return new EchoResponse("order-service", request.message(),
                CorrelationContext.getTraceId(), Instant.now().toString());
    }

    public record EchoRequest(String message) {
    }

    public record EchoResponse(String service, String echo, String traceId, String receivedAt) {
    }
}
