package parallax.vn.springbootdemo.application.order.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** Aggregated return figures for a customer, computed from order_db. */
public record ReturnHistoryStats(
        long totalReturns,
        BigDecimal totalReturnedAmount,
        String currency,
        long returnsLast30Days,
        Instant lastReturnAt) {
}
