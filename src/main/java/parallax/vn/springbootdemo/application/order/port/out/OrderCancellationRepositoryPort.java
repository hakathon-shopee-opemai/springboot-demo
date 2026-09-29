package parallax.vn.springbootdemo.application.order.port.out;

import java.util.Optional;
import parallax.vn.springbootdemo.domain.order.aggregate.OrderCancellation;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;

public interface OrderCancellationRepositoryPort {
    Optional<OrderCancellation> findByOrderId(OrderId orderId);

    OrderCancellation save(OrderCancellation cancellation);
}
