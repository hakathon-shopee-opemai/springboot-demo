package parallax.vn.springbootdemo.application.order.dto;

/** Result of a risk-service velocity adjustment (idempotent by operationId). */
public record VelocityAdjustResult(long previousCount, long newCount, boolean applied) {
}
