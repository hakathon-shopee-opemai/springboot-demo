package parallax.vn.springbootdemo.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds the {@code ratelimit.*} tree (fixed-window limiter). */
@ConfigurationProperties(prefix = "ratelimit")
public record RateLimitProperties(int capacity, long windowSeconds, boolean enabled) {
}
