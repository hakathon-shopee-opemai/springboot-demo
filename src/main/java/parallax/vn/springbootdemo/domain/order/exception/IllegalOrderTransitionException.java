package parallax.vn.springbootdemo.domain.order.exception;

import parallax.vn.springbootdemo.domain.order.vo.OrderStatus;
import parallax.vn.springbootdemo.domain.shared.exception.DomainException;

public class IllegalOrderTransitionException extends DomainException {
    public IllegalOrderTransitionException(OrderStatus from, OrderStatus to) {
        super("Illegal order transition: " + from + " -> " + to);
    }
}
