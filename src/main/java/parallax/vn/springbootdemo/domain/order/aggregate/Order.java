package parallax.vn.springbootdemo.domain.order.aggregate;

import java.time.Instant;
import java.util.List;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;
import parallax.vn.springbootdemo.domain.order.entity.OrderLine;
import parallax.vn.springbootdemo.domain.order.exception.IllegalOrderTransitionException;
import parallax.vn.springbootdemo.domain.order.exception.InvalidOrderException;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;
import parallax.vn.springbootdemo.domain.order.vo.OrderStatus;
import parallax.vn.springbootdemo.domain.order.vo.PaymentMethod;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecisionSnapshot;
import parallax.vn.springbootdemo.domain.shared.vo.Address;
import parallax.vn.springbootdemo.domain.shared.vo.Money;

/** Order aggregate root. Owns totals computation and lifecycle transitions. */
public class Order {

    private final OrderId id;
    private final String orderNumber;
    private final CustomerId customerId;
    private OrderStatus status;
    private final String currency;
    private final List<OrderLine> lines;
    private final Money subtotal;
    private final Money shippingFee;
    private final Money total;
    private final Address shippingAddress;
    private final PaymentMethod paymentMethod;
    private RiskDecisionSnapshot risk;
    private final Instant createdAt;
    private Instant updatedAt;
    private long version;

    private Order(OrderId id, String orderNumber, CustomerId customerId, OrderStatus status,
            String currency, List<OrderLine> lines, Money subtotal, Money shippingFee, Money total,
            Address shippingAddress, PaymentMethod paymentMethod, RiskDecisionSnapshot risk,
            Instant createdAt, Instant updatedAt, long version) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.status = status;
        this.currency = currency;
        this.lines = List.copyOf(lines);
        this.subtotal = subtotal;
        this.shippingFee = shippingFee;
        this.total = total;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.risk = risk;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    /** Factory enforcing invariants BR-1/BR-2; produces a PENDING order with totals computed. */
    public static Order place(String orderNumber, CustomerId customerId, String currency,
            List<OrderLine> lines, Address shippingAddress, PaymentMethod paymentMethod) {
        if (lines == null || lines.isEmpty()) {
            throw new InvalidOrderException("an order must have at least one line");
        }
        Money subtotal = Money.zero(currency);
        for (OrderLine line : lines) {
            if (!line.unitPrice().currency().equals(currency)) {
                throw new InvalidOrderException("all lines must use the order currency " + currency);
            }
            subtotal = subtotal.add(line.lineTotal());
        }
        Money shippingFee = Money.zero(currency);
        Money total = subtotal.add(shippingFee);
        Instant now = Instant.now();
        return new Order(OrderId.newId(), orderNumber, customerId, OrderStatus.PENDING, currency,
                lines, subtotal, shippingFee, total, shippingAddress, paymentMethod, null, now, now, 0L);
    }

    public static Order rehydrate(OrderId id, String orderNumber, CustomerId customerId,
            OrderStatus status, String currency, List<OrderLine> lines, Money subtotal,
            Money shippingFee, Money total, Address shippingAddress, PaymentMethod paymentMethod,
            RiskDecisionSnapshot risk, Instant createdAt, Instant updatedAt, long version) {
        return new Order(id, orderNumber, customerId, status, currency, lines, subtotal, shippingFee,
                total, shippingAddress, paymentMethod, risk, createdAt, updatedAt, version);
    }

    /** Apply the risk verdict and move to the resulting lifecycle state. */
    public void applyRisk(RiskDecisionSnapshot snapshot) {
        OrderStatus target = snapshot.decision().toOrderStatus();
        transitionTo(target);
        this.risk = snapshot;
    }

    public void cancel() {
        transitionTo(OrderStatus.CANCELLED);
    }

    private void transitionTo(OrderStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new IllegalOrderTransitionException(status, target);
        }
        this.status = target;
        this.updatedAt = Instant.now();
    }

    public boolean isRejected() {
        return status == OrderStatus.REJECTED;
    }

    public OrderId id() {
        return id;
    }

    public String orderNumber() {
        return orderNumber;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public OrderStatus status() {
        return status;
    }

    public String currency() {
        return currency;
    }

    public List<OrderLine> lines() {
        return lines;
    }

    public Money subtotal() {
        return subtotal;
    }

    public Money shippingFee() {
        return shippingFee;
    }

    public Money total() {
        return total;
    }

    public Address shippingAddress() {
        return shippingAddress;
    }

    public PaymentMethod paymentMethod() {
        return paymentMethod;
    }

    public RiskDecisionSnapshot risk() {
        return risk;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public long version() {
        return version;
    }
}
