package parallax.vn.springbootdemo.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import parallax.vn.springbootdemo.common.exception.code.ErrorCode;
import parallax.vn.springbootdemo.common.exception.model.ProblemDetails;
import parallax.vn.springbootdemo.config.properties.RateLimitProperties;

/** Fixed-window rate limiter for public /api/v1/** routes, backed by Redis INCR/EXPIRE. */
public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redis;
    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(StringRedisTemplate redis, RateLimitProperties properties, ObjectMapper objectMapper) {
        this.redis = redis;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !properties.enabled() || !request.getRequestURI().startsWith("/api/v1/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        long window = properties.windowSeconds();
        long windowStart = (Instant.now().getEpochSecond() / window) * window;
        String key = "ratelimit:" + principal(request) + ":" + windowStart;

        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, Duration.ofSeconds(window));
        }
        if (count != null && count > properties.capacity()) {
            long retryAfter = (windowStart + window) - Instant.now().getEpochSecond();
            response.setStatus(ErrorCode.RATE_LIMITED.getStatus().value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(Math.max(retryAfter, 1)));
            objectMapper.writeValue(response.getOutputStream(),
                    ProblemDetails.asMap(ErrorCode.RATE_LIMITED, "Rate limit exceeded"));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String principal(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return request.getRemoteAddr();
    }
}
