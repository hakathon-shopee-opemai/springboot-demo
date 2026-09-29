package parallax.vn.springbootdemo.adapter.out.cache.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import parallax.vn.springbootdemo.application.order.port.out.OrderDecisionCachePort;
import parallax.vn.springbootdemo.common.annotation.PersistenceAdapter;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecisionSnapshot;

/** Redis-backed decision + idempotency cache, plus cache invalidation on cancellation. */
@PersistenceAdapter
public class OrderDecisionCacheAdapter implements OrderDecisionCachePort {

    private static final Logger log = LoggerFactory.getLogger(OrderDecisionCacheAdapter.class);
    private static final Duration TTL = Duration.ofHours(24);
    private static final String CUSTOMER_HISTORY_CACHE = "customerHistory";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final CacheManager cacheManager;

    public OrderDecisionCacheAdapter(StringRedisTemplate redis, ObjectMapper objectMapper,
            CacheManager cacheManager) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.cacheManager = cacheManager;
    }

    @Override
    public Optional<RiskDecisionSnapshot> getDecision(OrderId id) {
        String json = redis.opsForValue().get(decisionKey(id));
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, RiskDecisionSnapshot.class));
        } catch (Exception e) {
            log.warn("failed to read cached decision for {}", id.value(), e);
            return Optional.empty();
        }
    }

    @Override
    public void putDecision(OrderId id, RiskDecisionSnapshot decision) {
        try {
            redis.opsForValue().set(decisionKey(id), objectMapper.writeValueAsString(decision), TTL);
        } catch (Exception e) {
            log.warn("failed to cache decision for {}", id.value(), e);
        }
    }

    @Override
    public Optional<OrderId> resolveIdempotencyKey(String key) {
        String id = redis.opsForValue().get(idemKey(key));
        return id == null ? Optional.empty() : Optional.of(OrderId.of(id));
    }

    @Override
    public void rememberIdempotencyKey(String key, OrderId id) {
        redis.opsForValue().set(idemKey(key), id.value().toString(), TTL);
    }

    @Override
    public void evictDecision(OrderId id) {
        redis.delete(decisionKey(id));
    }

    @Override
    public void evictCustomerHistory(CustomerId customerId) {
        Cache cache = cacheManager.getCache(CUSTOMER_HISTORY_CACHE);
        if (cache != null) {
            cache.evict(customerId.value().toString());
        }
    }

    @Override
    public Optional<OrderId> resolveCancelIdempotencyKey(String key) {
        String id = redis.opsForValue().get(cancelIdemKey(key));
        return id == null ? Optional.empty() : Optional.of(OrderId.of(id));
    }

    @Override
    public void rememberCancelIdempotencyKey(String key, OrderId id) {
        redis.opsForValue().set(cancelIdemKey(key), id.value().toString(), TTL);
    }

    private String decisionKey(OrderId id) {
        return "order:decision:" + id.value();
    }

    private String idemKey(String key) {
        return "order:idem:" + key;
    }

    private String cancelIdemKey(String key) {
        return "order:cancel:idem:" + key;
    }
}
