package parallax.vn.springbootdemo.domain.order.vo;

/** The risk engine's verdict on a return and how it maps onto the return lifecycle. */
public enum ReturnDecision {
    AUTO_REFUND,
    REVIEW,
    DENY;

    public ReturnStatus toReturnStatus() {
        return switch (this) {
            case AUTO_REFUND -> ReturnStatus.REFUNDED;
            case REVIEW -> ReturnStatus.UNDER_REVIEW;
            case DENY -> ReturnStatus.DENIED;
        };
    }
}
