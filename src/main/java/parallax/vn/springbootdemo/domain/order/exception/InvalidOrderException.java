package parallax.vn.springbootdemo.domain.order.exception;

import parallax.vn.springbootdemo.domain.shared.exception.DomainException;

public class InvalidOrderException extends DomainException {
    public InvalidOrderException(String message) {
        super(message);
    }
}
