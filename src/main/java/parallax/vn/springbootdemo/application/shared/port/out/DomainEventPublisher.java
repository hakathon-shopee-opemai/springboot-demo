package parallax.vn.springbootdemo.application.shared.port.out;

import parallax.vn.springbootdemo.domain.shared.event.DomainEvent;

/** Outbound port for publishing domain events out of the application core. */
public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
