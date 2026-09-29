package parallax.vn.springbootdemo.domain.order.vo;

/** The risk engine's verdict and how it maps onto the order lifecycle. */
public enum RiskDecision {
    APPROVE,
    REVIEW,
    REJECT;

    public OrderStatus toOrderStatus() {
        return switch (this) {
            case APPROVE -> OrderStatus.APPROVED;
            case REVIEW -> OrderStatus.ON_HOLD;
            case REJECT -> OrderStatus.REJECTED;
        };
    }
}
