package parallax.vn.springbootdemo.config.resilience;

import org.springframework.context.annotation.Configuration;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Turns on Spring 7 {@code @Retryable}/{@code @ConcurrencyLimit} and {@code @Scheduled} jobs. */
@Configuration
@EnableResilientMethods
@EnableScheduling
public class ResilienceConfig {
}
