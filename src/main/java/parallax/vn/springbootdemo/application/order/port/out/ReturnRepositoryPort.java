package parallax.vn.springbootdemo.application.order.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import parallax.vn.springbootdemo.application.order.dto.ReturnHistoryStats;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;
import parallax.vn.springbootdemo.domain.order.aggregate.OrderReturn;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;

public interface ReturnRepositoryPort {
    OrderReturn save(OrderReturn orderReturn);

    Optional<OrderReturn> findById(UUID id);

    /** True when an active (REFUNDED|UNDER_REVIEW) return already exists for the order. */
    boolean existsActiveReturn(OrderId orderId);

    /** Aggregated return figures for the customer; {@code since} bounds the rolling window. */
    ReturnHistoryStats statsByCustomer(CustomerId customerId, Instant since);
}
