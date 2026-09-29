package parallax.vn.springbootdemo.domain.order.exception;

import parallax.vn.springbootdemo.domain.shared.exception.DomainException;

/** Raised when a return violates a business invariant (state, window, amount, transition). */
public class ReturnNotAllowedException extends DomainException {
    public ReturnNotAllowedException(String message) {
        super(message);
    }
}
