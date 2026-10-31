package parallax.vn.springbootdemo.config.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import parallax.vn.springbootdemo.application.customer.dto.CustomerHistory;

/** Tunes the auto-configured RedisCacheManager: JSON values + per-cache TTLs. */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer cacheManagerCustomizer(ObjectMapper objectMapper) {
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)));
        return builder -> builder
                .cacheDefaults(defaults)
                .withCacheConfiguration("customerHistory", defaults.entryTtl(Duration.ofMinutes(5))
                        // Typed serializer: the generic one has no type info for this mapper and
                        // reads entries back as LinkedHashMap, which fails the CustomerHistory cast.
                        .serializeValuesWith(RedisSerializationContext.SerializationPair
                                .fromSerializer(new Jackson2JsonRedisSerializer<>(objectMapper, CustomerHistory.class))))
                .withCacheConfiguration("orderDecision", defaults.entryTtl(Duration.ofHours(24)));
    }
}
