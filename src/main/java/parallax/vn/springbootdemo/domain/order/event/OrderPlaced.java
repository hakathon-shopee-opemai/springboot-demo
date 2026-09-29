package parallax.vn.springbootdemo.domain.order.event;

import java.time.Instant;
import java.util.UUID;
import parallax.vn.springbootdemo.domain.order.vo.OrderStatus;
import parallax.vn.springbootdemo.domain.shared.event.DomainEvent;

/** Emitted after an order has been placed and risk-decided. */
public record OrderPlaced(UUID orderId, UUID customerId, OrderStatus status, Instant occurredAt)
        implements DomainEvent {
}
