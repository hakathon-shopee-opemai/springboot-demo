package parallax.vn.springbootdemo.adapter.out.persistence.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_cancellations")
@Getter
@Setter
@NoArgsConstructor
public class OrderCancellationEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "reason", length = 512)
    private String reason;

    @Column(name = "requested_by", length = 128)
    private String requestedBy;

    @Column(name = "saga_steps", nullable = false, columnDefinition = "text")
    private String sagaSteps;

    @Column(name = "idempotency_key", length = 128)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;
}
