package parallax.vn.springbootdemo.domain.shared.exception;

/** Base type for all domain rule violations. */
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
}
