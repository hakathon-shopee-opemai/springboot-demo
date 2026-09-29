package parallax.vn.springbootdemo.adapter.out.persistence.customer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class CustomerEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, length = 320, unique = true)
    private String email;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "phone", length = 32)
    private String phone;

    @Column(name = "account_status", nullable = false, length = 16)
    private String accountStatus;

    @Column(name = "tier", nullable = false, length = 16)
    private String tier;

    @Column(name = "lifetime_order_count", nullable = false)
    private long lifetimeOrderCount;

    @Column(name = "lifetime_gmv_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal lifetimeGmvAmount;

    @Column(name = "lifetime_gmv_currency", nullable = false, length = 3)
    private String lifetimeGmvCurrency;

    @Column(name = "last_order_at")
    private Instant lastOrderAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;
}
