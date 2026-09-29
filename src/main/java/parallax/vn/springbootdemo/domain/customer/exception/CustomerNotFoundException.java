package parallax.vn.springbootdemo.domain.customer.exception;

import parallax.vn.springbootdemo.domain.shared.exception.DomainException;

public class CustomerNotFoundException extends DomainException {
    public CustomerNotFoundException(String id) {
        super("Customer not found: " + id);
    }
}
