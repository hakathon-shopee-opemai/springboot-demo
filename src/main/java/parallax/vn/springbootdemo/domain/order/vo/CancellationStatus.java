package parallax.vn.springbootdemo.domain.order.vo;

/** Lifecycle of a cancellation saga. */
public enum CancellationStatus {
    IN_PROGRESS,
    COMPLETED,
    COMPENSATED,
    FAILED
}
