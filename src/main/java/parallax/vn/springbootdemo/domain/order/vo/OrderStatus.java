package parallax.vn.springbootdemo.domain.order.vo;

import java.util.Set;

/** Order lifecycle state with the allowed transition rules (docs/01 section 4). */
public enum OrderStatus {
    PENDING,
    APPROVED,
    ON_HOLD,
    REJECTED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case PENDING -> Set.of(APPROVED, ON_HOLD, REJECTED, CANCELLED).contains(target);
            case ON_HOLD -> Set.of(APPROVED, REJECTED, CANCELLED).contains(target);
            case APPROVED -> target == CANCELLED;
            case REJECTED, CANCELLED -> false;
        };
    }
}
