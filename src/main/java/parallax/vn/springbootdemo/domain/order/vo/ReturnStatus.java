package parallax.vn.springbootdemo.domain.order.vo;

/** Lifecycle of a return request; REFUNDED and UNDER_REVIEW are the "active" states. */
public enum ReturnStatus {
    REQUESTED,
    REFUNDED,
    UNDER_REVIEW,
    DENIED;

    public boolean canTransitionTo(ReturnStatus target) {
        return switch (this) {
            case REQUESTED -> target == REFUNDED || target == UNDER_REVIEW || target == DENIED;
            case UNDER_REVIEW -> target == REFUNDED || target == DENIED;
            case REFUNDED, DENIED -> false;
        };
    }

    /** Active returns block a second return on the same order (see ux_order_returns_active). */
    public boolean isActive() {
        return this == REFUNDED || this == UNDER_REVIEW;
    }
}
