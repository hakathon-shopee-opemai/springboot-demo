package parallax.vn.springbootdemo.application.order.usecase;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import parallax.vn.springbootdemo.application.order.dto.OrderRiskSnapshot;
import parallax.vn.springbootdemo.application.order.dto.PlaceOrderCommand;
import parallax.vn.springbootdemo.application.order.port.in.PlaceOrderUseCase;
import parallax.vn.springbootdemo.application.order.port.out.InventoryPort;
import parallax.vn.springbootdemo.application.order.port.out.OrderDecisionCachePort;
import parallax.vn.springbootdemo.application.order.port.out.OrderNumberPort;
import parallax.vn.springbootdemo.application.order.port.out.OrderRepositoryPort;
import parallax.vn.springbootdemo.application.order.port.out.RiskAssessmentPort;
import parallax.vn.springbootdemo.application.shared.port.out.DomainEventPublisher;
import parallax.vn.springbootdemo.common.annotation.RateLimited;
import parallax.vn.springbootdemo.common.annotation.UseCase;
import parallax.vn.springbootdemo.common.exception.code.ErrorCode;
import parallax.vn.springbootdemo.common.exception.model.ApplicationException;
import parallax.vn.springbootdemo.domain.customer.aggregate.Customer;
import parallax.vn.springbootdemo.domain.customer.exception.CustomerNotFoundException;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;
import parallax.vn.springbootdemo.application.customer.port.out.CustomerRepositoryPort;
import parallax.vn.springbootdemo.domain.order.aggregate.Order;
import parallax.vn.springbootdemo.domain.order.entity.OrderLine;
import parallax.vn.springbootdemo.domain.order.event.OrderPlaced;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;
import parallax.vn.springbootdemo.domain.order.vo.PaymentMethod;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecisionSnapshot;
import parallax.vn.springbootdemo.domain.shared.vo.Address;
import parallax.vn.springbootdemo.domain.shared.vo.Money;

/** Orchestrates order placement including the parallax hop to the risk-service. */
@UseCase
public class PlaceOrderService implements PlaceOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(PlaceOrderService.class);

    private final OrderRepositoryPort orders;
    private final CustomerRepositoryPort customers;
    private final OrderNumberPort orderNumbers;
    private final RiskAssessmentPort risk;
    private final OrderDecisionCachePort cache;
    private final InventoryPort inventory;
    private final DomainEventPublisher events;

    public PlaceOrderService(OrderRepositoryPort orders, CustomerRepositoryPort customers,
            OrderNumberPort orderNumbers, RiskAssessmentPort risk, OrderDecisionCachePort cache,
            InventoryPort inventory, DomainEventPublisher events) {
        this.orders = orders;
        this.customers = customers;
        this.orderNumbers = orderNumbers;
        this.risk = risk;
        this.cache = cache;
        this.inventory = inventory;
        this.events = events;
    }

    @Override
    @Transactional
    @RateLimited(key = "orders.place", permits = 30, windowSeconds = 60)
    public Order placeOrder(PlaceOrderCommand command) {
        // 1. idempotency short-circuit
        if (command.idempotencyKey() != null && !command.idempotencyKey().isBlank()) {
            var existing = cache.resolveIdempotencyKey(command.idempotencyKey());
            if (existing.isPresent()) {
                return orders.findById(existing.get())
                        .orElseThrow(() -> new ApplicationException(ErrorCode.INTERNAL,
                                "idempotent order missing"));
            }
        }

        // 2. load customer
        CustomerId customerId = CustomerId.of(command.customerId());
        Customer customer = customers.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(command.customerId().toString()));

        // 3. build PENDING order + totals
        List<OrderLine> lines = new ArrayList<>();
        for (PlaceOrderCommand.LineItem item : command.lines()) {
            lines.add(OrderLine.create(item.productId(), item.productName(),
                    Money.of(item.unitPrice(), command.currency()), item.quantity()));
        }
        Address address = command.shippingAddress() == null ? null
                : new Address(command.shippingAddress().line1(), command.shippingAddress().city(),
                        command.shippingAddress().country(), command.shippingAddress().postalCode());
        Order order = Order.place(orderNumbers.next(), customerId, command.currency(), lines, address,
                parsePaymentMethod(command.paymentMethod()));

        // 4. persist PENDING (stock is only held once risk has spoken)
        order = orders.save(order);

        // 5. parallax hop: risk assessment (fail-safe on error inside the adapter)
        OrderRiskSnapshot snapshot = toSnapshot(order, address);
        RiskDecisionSnapshot decision = risk.assess(snapshot);

        // 6. apply verdict; reserve stock line by line + stats only for non-rejected orders
        order.applyRisk(decision);
        if (!order.isRejected()) {
            for (OrderLine line : order.lines()) {
                inventory.reserveLine(order.id(), line);
            }
            customer.registerPlacedOrder(order.total(), Instant.now());
            customers.save(customer);
        }
        order = orders.save(order);

        // 7. cache decision + remember idempotency key (same-service call)
        rememberDecision(order, decision, command.idempotencyKey());

        // 8. publish event
        events.publish(new OrderPlaced(order.id().value(), customerId.value(), order.status(),
                Instant.now()));
        log.info("order placed number={} status={} score={}", order.orderNumber(), order.status(),
                decision.score());
        return order;
    }

    /**
     * Remembers the risk verdict and the idempotency key in its own transaction.
     * Called from {@link #placeOrder} on {@code this}: the self-invocation bypasses the
     * Spring proxy, so REQUIRES_NEW is silently ignored and the writes join the outer tx.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rememberDecision(Order order, RiskDecisionSnapshot decision, String idempotencyKey) {
        cache.putDecision(order.id(), decision);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            cache.rememberIdempotencyKey(idempotencyKey, order.id());
        }
    }

    /** Unknown values are client input errors (400), not server failures (500). */
    private static PaymentMethod parsePaymentMethod(String raw) {
        try {
            return PaymentMethod.parse(raw);
        } catch (IllegalArgumentException ex) {
            throw new ApplicationException(ErrorCode.VALIDATION_ERROR,
                    "Unsupported paymentMethod '" + raw + "'. Allowed values: "
                            + java.util.Arrays.toString(PaymentMethod.values()));
        }
    }

    private OrderRiskSnapshot toSnapshot(Order order, Address address) {
        List<OrderRiskSnapshot.LineSnapshot> lines = order.lines().stream()
                .map(l -> new OrderRiskSnapshot.LineSnapshot(l.productId(), l.quantity(),
                        l.unitPrice().amount()))
                .toList();
        String shippingCountry = address == null ? null : address.country();
        return new OrderRiskSnapshot(order.id().value(), order.customerId().value(),
                order.total().amount(), order.currency(), shippingCountry, lines);
    }
}
