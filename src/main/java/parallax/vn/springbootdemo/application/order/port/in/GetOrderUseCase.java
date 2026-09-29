package parallax.vn.springbootdemo.application.order.port.in;

import parallax.vn.springbootdemo.domain.order.aggregate.Order;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;

public interface GetOrderUseCase {
    Order getOrder(OrderId id);
}
