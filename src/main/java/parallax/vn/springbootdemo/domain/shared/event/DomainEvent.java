package parallax.vn.springbootdemo.domain.shared.event;

import java.time.Instant;

/** Marker for domain events. */
public interface DomainEvent {
    Instant occurredAt();
}
