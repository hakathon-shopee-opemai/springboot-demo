package parallax.vn.springbootdemo.config.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds {@code clients.risk-service.*} — the outbound parallax hop target. */
@ConfigurationProperties(prefix = "clients.risk-service")
public record RiskClientProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {
}
