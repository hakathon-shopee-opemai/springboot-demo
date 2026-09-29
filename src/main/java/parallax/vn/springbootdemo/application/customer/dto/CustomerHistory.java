package parallax.vn.springbootdemo.application.customer.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Customer order-history projection served on the inbound parallax hop. Serializable for Redis cache. */
public record CustomerHistory(
        UUID customerId,
        String accountStatus,
        String tier,
        long lifetimeOrderCount,
        BigDecimal lifetimeGmv,
        String currency,
        Instant lastOrderAt) implements Serializable {
}
