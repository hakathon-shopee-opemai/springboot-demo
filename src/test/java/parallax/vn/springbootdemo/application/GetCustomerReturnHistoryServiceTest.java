package parallax.vn.springbootdemo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parallax.vn.springbootdemo.application.order.dto.CustomerReturnHistory;
import parallax.vn.springbootdemo.application.order.dto.ReturnHistoryStats;
import parallax.vn.springbootdemo.application.order.port.out.OrderRepositoryPort;
import parallax.vn.springbootdemo.application.order.port.out.ReturnRepositoryPort;
import parallax.vn.springbootdemo.application.order.usecase.GetCustomerReturnHistoryService;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;

class GetCustomerReturnHistoryServiceTest {

    private OrderRepositoryPort orders;
    private ReturnRepositoryPort returns;
    private GetCustomerReturnHistoryService service;

    @BeforeEach
    void setUp() {
        orders = mock(OrderRepositoryPort.class);
        returns = mock(ReturnRepositoryPort.class);
        service = new GetCustomerReturnHistoryService(orders, returns);
    }

    @Test
    void aggregates_and_computes_return_rate() {
        CustomerId id = CustomerId.newId();
        Instant last = Instant.parse("2026-09-20T10:00:00Z");
        when(orders.countByCustomerId(eq(id))).thenReturn(20L);
        when(returns.statsByCustomer(eq(id), any())).thenReturn(
                new ReturnHistoryStats(5L, new BigDecimal("480.00"), "USD", 3L, last));

        CustomerReturnHistory history = service.getReturnHistory(id);

        assertThat(history.customerId()).isEqualTo(id.value());
        assertThat(history.totalOrders()).isEqualTo(20L);
        assertThat(history.totalReturns()).isEqualTo(5L);
        assertThat(history.returnRate()).isEqualByComparingTo("0.25");
        assertThat(history.totalReturnedAmount()).isEqualByComparingTo("480.00");
        assertThat(history.currency()).isEqualTo("USD");
        assertThat(history.returnsLast30Days()).isEqualTo(3L);
        assertThat(history.lastReturnAt()).isEqualTo(last);
    }

    @Test
    void unknown_customer_yields_zero_rate_and_null_last_return() {
        CustomerId id = CustomerId.newId();
        when(orders.countByCustomerId(eq(id))).thenReturn(0L);
        when(returns.statsByCustomer(eq(id), any())).thenReturn(
                new ReturnHistoryStats(0L, BigDecimal.ZERO, "USD", 0L, null));

        CustomerReturnHistory history = service.getReturnHistory(id);

        assertThat(history.totalOrders()).isZero();
        assertThat(history.totalReturns()).isZero();
        assertThat(history.returnRate()).isEqualByComparingTo("0.0");
        assertThat(history.lastReturnAt()).isNull();
    }
}
