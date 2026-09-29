package parallax.vn.springbootdemo.config.persistence;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Enables JPA auditing (createdAt/updatedAt population once entities exist). */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
