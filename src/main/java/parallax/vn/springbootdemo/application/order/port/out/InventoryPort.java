package parallax.vn.springbootdemo.application.order.port.out;

import java.util.List;
import parallax.vn.springbootdemo.domain.order.entity.OrderLine;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;

/** Simplified inventory reservation (stubbed for the demo). */
public interface InventoryPort {
    void reserve(OrderId orderId, List<OrderLine> lines);

    void release(OrderId orderId);
}
