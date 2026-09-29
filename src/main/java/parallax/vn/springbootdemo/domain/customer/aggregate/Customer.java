package parallax.vn.springbootdemo.domain.customer.aggregate;

import java.time.Instant;
import parallax.vn.springbootdemo.domain.customer.vo.AccountStatus;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerTier;
import parallax.vn.springbootdemo.domain.customer.vo.Email;
import parallax.vn.springbootdemo.domain.shared.vo.Money;

/** Customer aggregate root: identity, standing, and lifetime order statistics. */
public class Customer {

    private final CustomerId id;
    private final Email email;
    private String fullName;
    private String phone;
    private AccountStatus status;
    private CustomerTier tier;
    private long lifetimeOrderCount;
    private Money lifetimeGmv;
    private Instant lastOrderAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private long version;

    private Customer(CustomerId id, Email email, String fullName, String phone, AccountStatus status,
            CustomerTier tier, long lifetimeOrderCount, Money lifetimeGmv, Instant lastOrderAt,
            Instant createdAt, Instant updatedAt, long version) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.status = status;
        this.tier = tier;
        this.lifetimeOrderCount = lifetimeOrderCount;
        this.lifetimeGmv = lifetimeGmv;
        this.lastOrderAt = lastOrderAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    /** Factory for a brand-new customer. */
    public static Customer register(Email email, String fullName, String phone,
            CustomerTier tier, AccountStatus status) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("fullName is required");
        }
        Instant now = Instant.now();
        return new Customer(CustomerId.newId(), email, fullName, phone,
                status == null ? AccountStatus.ACTIVE : status,
                tier == null ? CustomerTier.STANDARD : tier,
                0L, Money.zero("USD"), null, now, now, 0L);
    }

    /** Rehydrate from persistence. */
    public static Customer rehydrate(CustomerId id, Email email, String fullName, String phone,
            AccountStatus status, CustomerTier tier, long lifetimeOrderCount, Money lifetimeGmv,
            Instant lastOrderAt, Instant createdAt, Instant updatedAt, long version) {
        return new Customer(id, email, fullName, phone, status, tier, lifetimeOrderCount,
                lifetimeGmv, lastOrderAt, createdAt, updatedAt, version);
    }

    /** Record a successfully placed (non-rejected) order against lifetime stats. */
    public void registerPlacedOrder(Money orderTotal, Instant when) {
        this.lifetimeOrderCount += 1;
        if (this.lifetimeGmv.currency().equals(orderTotal.currency())) {
            this.lifetimeGmv = this.lifetimeGmv.add(orderTotal);
        } else {
            this.lifetimeGmv = Money.of(this.lifetimeGmv.amount().add(orderTotal.amount()),
                    this.lifetimeGmv.currency());
        }
        this.lastOrderAt = when;
        this.updatedAt = Instant.now();
    }

    /** Reverse a previously counted order (e.g. on cancellation); never goes negative. */
    public void reverseOrder(Money orderTotal) {
        if (this.lifetimeOrderCount > 0) {
            this.lifetimeOrderCount -= 1;
        }
        if (this.lifetimeGmv.currency().equals(orderTotal.currency())) {
            Money reduced = this.lifetimeGmv.subtract(orderTotal);
            this.lifetimeGmv = reduced.isNegative() ? Money.zero(this.lifetimeGmv.currency()) : reduced;
        }
        this.updatedAt = Instant.now();
    }

    public boolean isBlocked() {
        return status == AccountStatus.BLOCKED;
    }

    public CustomerId id() {
        return id;
    }

    public Email email() {
        return email;
    }

    public String fullName() {
        return fullName;
    }

    public String phone() {
        return phone;
    }

    public AccountStatus status() {
        return status;
    }

    public CustomerTier tier() {
        return tier;
    }

    public long lifetimeOrderCount() {
        return lifetimeOrderCount;
    }

    public Money lifetimeGmv() {
        return lifetimeGmv;
    }

    public Instant lastOrderAt() {
        return lastOrderAt;
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
