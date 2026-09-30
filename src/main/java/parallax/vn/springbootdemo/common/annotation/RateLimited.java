package parallax.vn.springbootdemo.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Per-method fixed-window rate limit, enforced by {@code RateLimitedAspect} on Redis.
 * Complements the global {@code RateLimitFilter}: this one guards a single business operation.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {

    /** Logical bucket name, e.g. {@code orders.place}. */
    String key();

    /** Calls allowed per window (per bucket). */
    int permits();

    /** Window length in seconds. */
    long windowSeconds() default 60;
}
