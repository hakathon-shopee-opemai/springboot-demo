package parallax.vn.springbootdemo.adapter.out.persistence.order.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "order_number", nullable = false, length = 32, unique = true)
    private String orderNumber;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "subtotal_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal subtotalAmount;

    @Column(name = "shipping_fee_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal shippingFeeAmount;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "ship_line1", length = 255)
    private String shipLine1;

    @Column(name = "ship_city", length = 128)
    private String shipCity;

    @Column(name = "ship_country", length = 64)
    private String shipCountry;

    @Column(name = "ship_postal_code", length = 32)
    private String shipPostalCode;

    @Column(name = "payment_method", nullable = false, length = 16)
    private String paymentMethod;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "risk_level", length = 8)
    private String riskLevel;

    @Column(name = "risk_decision", length = 8)
    private String riskDecision;

    @Column(name = "risk_assessment_id")
    private UUID riskAssessmentId;

    @Column(name = "risk_assessed_at")
    private Instant riskAssessedAt;

    @Column(name = "idempotency_key", length = 128, unique = true)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.EAGER)
    private List<OrderLineEntity> lines = new ArrayList<>();

    public void addLine(OrderLineEntity line) {
        line.setOrder(this);
        this.lines.add(line);
    }
}
