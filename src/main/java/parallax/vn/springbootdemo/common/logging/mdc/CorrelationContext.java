package parallax.vn.springbootdemo.common.logging.mdc;

import org.slf4j.MDC;

/** Correlation id helpers backed by SLF4J MDC. Keys match logback-spring.xml patterns. */
public final class CorrelationContext {

    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";
    public static final String USER_ID = "userId";
    public static final String HEADER = "X-Trace-Id";

    private CorrelationContext() {
    }

    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    public static void setSpanId(String spanId) {
        MDC.put(SPAN_ID, spanId);
    }

    public static void setUserId(String userId) {
        if (userId != null) {
            MDC.put(USER_ID, userId);
        }
    }

    public static void clear() {
        MDC.remove(TRACE_ID);
        MDC.remove(SPAN_ID);
        MDC.remove(USER_ID);
    }
}
