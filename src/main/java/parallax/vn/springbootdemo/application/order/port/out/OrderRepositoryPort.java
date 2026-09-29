package parallax.vn.springbootdemo.application.order.port.out;

import java.util.Optional;
import parallax.vn.springbootdemo.domain.order.aggregate.Order;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;

public interface OrderRepositoryPort {
    Order save(Order order);

    Optional<Order> findById(OrderId id);
}
