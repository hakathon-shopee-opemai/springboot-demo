package parallax.vn.springbootdemo.domain.order.vo;

/** Why a customer is returning an order (contract §2). */
public enum ReturnReason {
    DEFECTIVE,
    NOT_AS_DESCRIBED,
    NO_LONGER_NEEDED,
    ITEM_NOT_RECEIVED,
    WRONG_ITEM,
    OTHER
}
