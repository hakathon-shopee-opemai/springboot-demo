package parallax.vn.springbootdemo.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds the {@code security.*} tree. */
@ConfigurationProperties(prefix = "security")
public record SecurityProperties(String serviceSharedSecret, Jwt jwt, DemoUser demoUser) {

    public record Jwt(String secret, String issuer, long ttlSeconds) {
    }

    public record DemoUser(String username, String password) {
    }
}
