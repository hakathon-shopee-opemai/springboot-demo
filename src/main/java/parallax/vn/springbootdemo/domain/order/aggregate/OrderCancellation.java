package parallax.vn.springbootdemo.domain.order.aggregate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import parallax.vn.springbootdemo.domain.order.vo.CancellationStatus;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;
import parallax.vn.springbootdemo.domain.order.vo.SagaStep;

/** Persisted saga log for an order cancellation: state, reason, and step outcomes. */
public class OrderCancellation {

    private final UUID id;
    private final OrderId orderId;
    private CancellationStatus status;
    private final String reason;
    private final String requestedBy;
    private final List<SagaStep> steps;
    private final String idempotencyKey;
    private final Instant createdAt;
    private Instant updatedAt;
    private long version;

    private OrderCancellation(UUID id, OrderId orderId, CancellationStatus status, String reason,
            String requestedBy, List<SagaStep> steps, String idempotencyKey, Instant createdAt,
            Instant updatedAt, long version) {
        this.id = id;
        this.orderId = orderId;
        this.status = status;
        this.reason = reason;
        this.requestedBy = requestedBy;
        this.steps = new ArrayList<>(steps);
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public static OrderCancellation start(OrderId orderId, String reason, String requestedBy,
            String idempotencyKey) {
        Instant now = Instant.now();
        return new OrderCancellation(UUID.randomUUID(), orderId, CancellationStatus.IN_PROGRESS,
                reason, requestedBy, new ArrayList<>(), idempotencyKey, now, now, 0L);
    }

    public static OrderCancellation rehydrate(UUID id, OrderId orderId, CancellationStatus status,
            String reason, String requestedBy, List<SagaStep> steps, String idempotencyKey,
            Instant createdAt, Instant updatedAt, long version) {
        return new OrderCancellation(id, orderId, status, reason, requestedBy, steps, idempotencyKey,
                createdAt, updatedAt, version);
    }

    public void recordStep(SagaStep step) {
        this.steps.add(step);
        this.updatedAt = Instant.now();
    }

    public void complete() {
        this.status = CancellationStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void compensated() {
        this.status = CancellationStatus.COMPENSATED;
        this.updatedAt = Instant.now();
    }

    public void failed() {
        this.status = CancellationStatus.FAILED;
        this.updatedAt = Instant.now();
    }

    public boolean isCompleted() {
        return status == CancellationStatus.COMPLETED;
    }

    public boolean isInProgress() {
        return status == CancellationStatus.IN_PROGRESS;
    }

    public UUID id() {
        return id;
    }

    public OrderId orderId() {
        return orderId;
    }

    public CancellationStatus status() {
        return status;
    }

    public String reason() {
        return reason;
    }

    public String requestedBy() {
        return requestedBy;
    }

    public List<SagaStep> steps() {
        return List.copyOf(steps);
    }

    public String idempotencyKey() {
        return idempotencyKey;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public long version() {
        return version;
    }
}
