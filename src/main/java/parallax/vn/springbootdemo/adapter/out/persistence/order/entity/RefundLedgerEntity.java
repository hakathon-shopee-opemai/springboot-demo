package parallax.vn.springbootdemo.adapter.out.persistence.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "refund_ledger")
@Getter
@Setter
@NoArgsConstructor
public class RefundLedgerEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "return_id", nullable = false, unique = true)
    private UUID returnId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
