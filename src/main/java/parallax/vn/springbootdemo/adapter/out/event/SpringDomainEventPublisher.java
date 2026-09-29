package parallax.vn.springbootdemo.adapter.out.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import parallax.vn.springbootdemo.application.shared.port.out.DomainEventPublisher;
import parallax.vn.springbootdemo.domain.shared.event.DomainEvent;

/** Bridges domain events onto Spring's ApplicationEventPublisher. */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher delegate;

    public SpringDomainEventPublisher(ApplicationEventPublisher delegate) {
        this.delegate = delegate;
    }

    @Override
    public void publish(DomainEvent event) {
        delegate.publishEvent(event);
    }
}
