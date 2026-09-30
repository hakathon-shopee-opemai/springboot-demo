package parallax.vn.springbootdemo.application.order.port.out;

import parallax.vn.springbootdemo.domain.order.entity.OrderLine;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;

/** Simplified inventory reservation (stubbed for the demo); stock is held line by line. */
public interface InventoryPort {
    void reserveLine(OrderId orderId, OrderLine line);

    void release(OrderId orderId);
}
