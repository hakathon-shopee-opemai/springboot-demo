package parallax.vn.springbootdemo.common.exception.model;

import lombok.Getter;
import parallax.vn.springbootdemo.common.exception.code.ErrorCode;

/** Base application exception carrying a stable {@link ErrorCode}. */
@Getter
public class ApplicationException extends RuntimeException {

    private final ErrorCode code;

    public ApplicationException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ApplicationException(ErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
