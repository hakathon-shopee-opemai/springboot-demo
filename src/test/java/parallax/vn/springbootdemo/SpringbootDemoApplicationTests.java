package parallax.vn.springbootdemo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Full-context smoke test. Requires a live Postgres + Redis (Flyway migration, Redis cache),
 * so it is disabled for offline unit runs and exercised via `docker compose up`.
 */
@Disabled("requires Postgres + Redis; run through docker compose, not the offline unit suite")
@SpringBootTest
class SpringbootDemoApplicationTests {

    @Test
    void contextLoads() {
    }
}
