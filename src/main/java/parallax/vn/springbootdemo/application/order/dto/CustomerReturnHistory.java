package parallax.vn.springbootdemo.application.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Authoritative customer return-history projection served on the inbound parallax hop (contract §2 Hop B). */
public record CustomerReturnHistory(
        UUID customerId,
        long totalOrders,
        long totalReturns,
        BigDecimal returnRate,
        BigDecimal totalReturnedAmount,
        String currency,
        long returnsLast30Days,
        Instant lastReturnAt) {
}
