package parallax.vn.springbootdemo.adapter.out.cache.order;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import parallax.vn.springbootdemo.application.order.port.out.DistributedLockPort;
import parallax.vn.springbootdemo.common.annotation.PersistenceAdapter;

/** Redis distributed lock: SET key token NX PX ttl; release via compare-and-delete Lua. */
@PersistenceAdapter
public class RedisDistributedLockAdapter implements DistributedLockPort {

    private static final DefaultRedisScript<Long> UNLOCK = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) "
                    + "else return 0 end",
            Long.class);

    private final StringRedisTemplate redis;

    public RedisDistributedLockAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public Optional<String> tryLock(String key, Duration ttl) {
        String token = UUID.randomUUID().toString();
        Boolean ok = redis.opsForValue().setIfAbsent(key, token, ttl);
        return Boolean.TRUE.equals(ok) ? Optional.of(token) : Optional.empty();
    }

    @Override
    public void unlock(String key, String token) {
        redis.execute(UNLOCK, List.of(key), token);
    }
}
