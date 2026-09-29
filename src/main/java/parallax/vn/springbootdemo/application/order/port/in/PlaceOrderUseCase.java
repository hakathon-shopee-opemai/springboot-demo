package parallax.vn.springbootdemo.application.order.port.in;

import parallax.vn.springbootdemo.application.order.dto.PlaceOrderCommand;
import parallax.vn.springbootdemo.domain.order.aggregate.Order;

public interface PlaceOrderUseCase {
    Order placeOrder(PlaceOrderCommand command);
}
