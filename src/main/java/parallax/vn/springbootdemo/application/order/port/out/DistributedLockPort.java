package parallax.vn.springbootdemo.application.order.port.out;

import java.time.Duration;
import java.util.Optional;

/** Best-effort distributed mutex (Redis SET NX + compare-and-delete release). */
public interface DistributedLockPort {
    /** @return the lock token if acquired, empty if already held by someone else. */
    Optional<String> tryLock(String key, Duration ttl);

    /** Releases the lock only if {@code token} still owns it. */
    void unlock(String key, String token);
}
