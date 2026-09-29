package parallax.vn.springbootdemo.common.exception.model;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ProblemDetail;
import parallax.vn.springbootdemo.common.exception.code.ErrorCode;
import parallax.vn.springbootdemo.common.logging.mdc.CorrelationContext;

/** Builds RFC-7807 payloads, enriched with our {@code code}, {@code traceId} and {@code timestamp}. */
public final class ProblemDetails {

    private ProblemDetails() {
    }

    public static ProblemDetail of(ErrorCode code, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(code.getStatus(), detail);
        pd.setTitle(code.name());
        pd.setProperty("code", code.name());
        pd.setProperty("traceId", CorrelationContext.getTraceId());
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }

    /** Flat map variant for filters that serialize before the MVC ProblemDetail machinery runs. */
    public static Map<String, Object> asMap(ErrorCode code, String detail) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "about:blank");
        map.put("title", code.name());
        map.put("status", code.getStatus().value());
        map.put("detail", detail);
        map.put("code", code.name());
        map.put("traceId", CorrelationContext.getTraceId());
        map.put("timestamp", Instant.now().toString());
        return map;
    }
}
