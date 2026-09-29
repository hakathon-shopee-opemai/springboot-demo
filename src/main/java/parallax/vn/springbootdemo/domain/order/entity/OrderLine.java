package parallax.vn.springbootdemo.domain.order.entity;

import parallax.vn.springbootdemo.domain.order.exception.InvalidOrderException;
import parallax.vn.springbootdemo.domain.order.vo.OrderLineId;
import parallax.vn.springbootdemo.domain.shared.vo.Money;

/** Child entity of the Order aggregate: one product line. */
public class OrderLine {

    private final OrderLineId id;
    private final String productId;
    private final String productName;
    private final Money unitPrice;
    private final int quantity;
    private final Money lineTotal;

    private OrderLine(OrderLineId id, String productId, String productName, Money unitPrice,
            int quantity, Money lineTotal) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    public static OrderLine create(String productId, String productName, Money unitPrice, int quantity) {
        if (productId == null || productId.isBlank()) {
            throw new InvalidOrderException("line productId is required");
        }
        if (quantity < 1) {
            throw new InvalidOrderException("line quantity must be >= 1");
        }
        if (unitPrice.isNegative()) {
            throw new InvalidOrderException("line unitPrice must be >= 0");
        }
        return new OrderLine(OrderLineId.newId(), productId, productName, unitPrice, quantity,
                unitPrice.multiply(quantity));
    }

    public static OrderLine rehydrate(OrderLineId id, String productId, String productName,
            Money unitPrice, int quantity, Money lineTotal) {
        return new OrderLine(id, productId, productName, unitPrice, quantity, lineTotal);
    }

    public OrderLineId id() {
        return id;
    }

    public String productId() {
        return productId;
    }

    public String productName() {
        return productName;
    }

    public Money unitPrice() {
        return unitPrice;
    }

    public int quantity() {
        return quantity;
    }

    public Money lineTotal() {
        return lineTotal;
    }
}
