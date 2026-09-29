package parallax.vn.springbootdemo.application.order.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import org.springframework.transaction.annotation.Transactional;
import parallax.vn.springbootdemo.application.order.dto.CustomerReturnHistory;
import parallax.vn.springbootdemo.application.order.dto.ReturnHistoryStats;
import parallax.vn.springbootdemo.application.order.port.in.GetCustomerReturnHistoryUseCase;
import parallax.vn.springbootdemo.application.order.port.out.OrderRepositoryPort;
import parallax.vn.springbootdemo.application.order.port.out.ReturnRepositoryPort;
import parallax.vn.springbootdemo.common.annotation.UseCase;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;

/** Serves the inbound parallax hop: authoritative customer return history from order_db. */
@UseCase
public class GetCustomerReturnHistoryService implements GetCustomerReturnHistoryUseCase {

    private static final Duration WINDOW = Duration.ofDays(30);

    private final OrderRepositoryPort orders;
    private final ReturnRepositoryPort returns;

    public GetCustomerReturnHistoryService(OrderRepositoryPort orders, ReturnRepositoryPort returns) {
        this.orders = orders;
        this.returns = returns;
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerReturnHistory getReturnHistory(CustomerId customerId) {
        long totalOrders = orders.countByCustomerId(customerId);
        ReturnHistoryStats stats = returns.statsByCustomer(customerId, Instant.now().minus(WINDOW));
        BigDecimal returnRate = BigDecimal.valueOf(stats.totalReturns())
                .divide(BigDecimal.valueOf(Math.max(totalOrders, 1L)), 4, RoundingMode.HALF_UP);
        return new CustomerReturnHistory(customerId.value(), totalOrders, stats.totalReturns(),
                returnRate, stats.totalReturnedAmount(), stats.currency(), stats.returnsLast30Days(),
                stats.lastReturnAt());
    }
}
