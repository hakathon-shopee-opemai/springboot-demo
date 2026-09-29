package parallax.vn.springbootdemo.application.order.port.in;

import parallax.vn.springbootdemo.application.order.dto.CancelOrderCommand;
import parallax.vn.springbootdemo.application.order.dto.CancellationResult;

public interface CancelOrderUseCase {
    CancellationResult cancelOrder(CancelOrderCommand command);
}
