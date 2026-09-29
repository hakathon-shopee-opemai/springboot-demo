package parallax.vn.springbootdemo.application.order.port.out;

import parallax.vn.springbootdemo.domain.order.vo.OrderId;

/** Payment refund (stubbed for the demo); compensatable. */
public interface RefundPort {
    void refund(OrderId orderId);

    void reverseRefund(OrderId orderId);
}
