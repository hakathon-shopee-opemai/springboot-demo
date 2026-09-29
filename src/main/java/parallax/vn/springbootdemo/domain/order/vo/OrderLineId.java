package parallax.vn.springbootdemo.domain.order.vo;

import java.util.Objects;
import java.util.UUID;

public record OrderLineId(UUID value) {
    public OrderLineId {
        Objects.requireNonNull(value, "order line id");
    }

    public static OrderLineId newId() {
        return new OrderLineId(UUID.randomUUID());
    }

    public static OrderLineId of(UUID value) {
        return new OrderLineId(value);
    }
}
