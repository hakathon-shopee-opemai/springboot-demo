package parallax.vn.springbootdemo.config.ratelimit;

import java.time.Duration;
import java.time.Instant;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import parallax.vn.springbootdemo.common.annotation.RateLimited;
import parallax.vn.springbootdemo.common.exception.code.ErrorCode;
import parallax.vn.springbootdemo.common.exception.model.ApplicationException;

/** Enforces {@link RateLimited} with a Redis INCR/EXPIRE fixed window per bucket. */
@Aspect
@Component
public class RateLimitedAspect {

    private final StringRedisTemplate redis;

    public RateLimitedAspect(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Around("@annotation(limit)")
    public Object enforce(ProceedingJoinPoint pjp, RateLimited limit) throws Throwable {
        long window = limit.windowSeconds();
        long windowStart = (Instant.now().getEpochSecond() / window) * window;
        String key = "ratelimit:op:" + limit.key() + ":" + windowStart;

        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, Duration.ofSeconds(window));
        }
        if (count != null && count > limit.permits()) {
            throw new ApplicationException(ErrorCode.RATE_LIMITED,
                    "too many " + limit.key() + " calls, retry in the next window");
        }
        return pjp.proceed();
    }
}
