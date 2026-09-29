package parallax.vn.springbootdemo.adapter.in.web.customer.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String email,
        String fullName,
        String phone,
        String tier,
        String accountStatus,
        long lifetimeOrderCount,
        BigDecimal lifetimeGmv,
        String currency,
        Instant createdAt) {
}
