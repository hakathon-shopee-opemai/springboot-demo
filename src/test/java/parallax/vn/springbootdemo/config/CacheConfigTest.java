package parallax.vn.springbootdemo.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import parallax.vn.springbootdemo.application.customer.dto.CustomerHistory;
import parallax.vn.springbootdemo.config.cache.CacheConfig;
import parallax.vn.springbootdemo.config.web.JacksonConfig;

class CacheConfigTest {

    @Test
    void customerHistoryCacheReadsBackAsCustomerHistoryNotMap() {
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder();
        new CacheConfig().cacheManagerCustomizer(new JacksonConfig().objectMapper()).customize(builder);
        RedisCacheConfiguration config = builder.getCacheConfigurationFor("customerHistory").orElseThrow();

        CustomerHistory original = new CustomerHistory(UUID.randomUUID(), "ACTIVE", "GOLD", 3L,
                new BigDecimal("120.50"), "USD", Instant.parse("2026-01-01T00:00:00Z"));

        ByteBuffer stored = config.getValueSerializationPair().write(original);
        Object restored = config.getValueSerializationPair().read(stored);

        assertThat(restored).isInstanceOf(CustomerHistory.class).isEqualTo(original);
    }
}
