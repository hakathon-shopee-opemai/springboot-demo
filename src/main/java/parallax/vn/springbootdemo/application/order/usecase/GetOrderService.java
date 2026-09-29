package parallax.vn.springbootdemo.application.order.usecase;

import org.springframework.transaction.annotation.Transactional;
import parallax.vn.springbootdemo.application.order.port.in.GetOrderUseCase;
import parallax.vn.springbootdemo.application.order.port.out.OrderRepositoryPort;
import parallax.vn.springbootdemo.common.annotation.UseCase;
import parallax.vn.springbootdemo.common.exception.code.ErrorCode;
import parallax.vn.springbootdemo.common.exception.model.ApplicationException;
import parallax.vn.springbootdemo.domain.order.aggregate.Order;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;

@UseCase
public class GetOrderService implements GetOrderUseCase {

    private final OrderRepositoryPort orders;

    public GetOrderService(OrderRepositoryPort orders) {
        this.orders = orders;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrder(OrderId id) {
        return orders.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND,
                        "order not found: " + id.value()));
    }
}
