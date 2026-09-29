package parallax.vn.springbootdemo.common.exception.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.stream.Collectors;
import parallax.vn.springbootdemo.common.exception.code.ErrorCode;
import parallax.vn.springbootdemo.common.exception.model.ApplicationException;
import parallax.vn.springbootdemo.common.exception.model.ProblemDetails;
import parallax.vn.springbootdemo.domain.customer.exception.CustomerNotFoundException;
import parallax.vn.springbootdemo.domain.shared.exception.DomainException;

/** Maps exceptions to RFC-7807 problem+json responses. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fieldName(fe) + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(ErrorCode.VALIDATION_ERROR, detail.isEmpty() ? "Validation failed" : detail);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuth(AuthenticationException ex) {
        return build(ErrorCode.UNAUTHENTICATED, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleDenied(AccessDeniedException ex) {
        return build(ErrorCode.ACCESS_DENIED, ex.getMessage());
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ProblemDetail> handleApp(ApplicationException ex) {
        return build(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleCustomerNotFound(CustomerNotFoundException ex) {
        return build(ErrorCode.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomain(DomainException ex) {
        return build(ErrorCode.BUSINESS_RULE, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return build(ErrorCode.INTERNAL, "Unexpected error");
    }

    private static String fieldName(FieldError fe) {
        return fe.getField();
    }

    private static ResponseEntity<ProblemDetail> build(ErrorCode code, String detail) {
        return ResponseEntity.status(code.getStatus())
                .body(ProblemDetails.of(code, detail));
    }
}
