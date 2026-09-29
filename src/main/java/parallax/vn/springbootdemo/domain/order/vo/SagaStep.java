package parallax.vn.springbootdemo.domain.order.vo;

/** One saga step and its outcome (PENDING|DONE|COMPENSATED|FAILED). */
public record SagaStep(String name, String status) {
    public static SagaStep done(String name) {
        return new SagaStep(name, "DONE");
    }

    public static SagaStep compensated(String name) {
        return new SagaStep(name, "COMPENSATED");
    }

    public static SagaStep failed(String name) {
        return new SagaStep(name, "FAILED");
    }
}
