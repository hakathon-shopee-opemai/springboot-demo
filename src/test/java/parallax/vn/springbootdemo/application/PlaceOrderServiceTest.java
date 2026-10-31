package parallax.vn.springbootdemo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parallax.vn.springbootdemo.application.customer.port.out.CustomerRepositoryPort;
import parallax.vn.springbootdemo.application.order.dto.PlaceOrderCommand;
import parallax.vn.springbootdemo.application.order.port.out.InventoryPort;
import parallax.vn.springbootdemo.application.order.port.out.OrderDecisionCachePort;
import parallax.vn.springbootdemo.application.order.port.out.OrderNumberPort;
import parallax.vn.springbootdemo.application.order.port.out.OrderRepositoryPort;
import parallax.vn.springbootdemo.application.order.port.out.RiskAssessmentPort;
import parallax.vn.springbootdemo.application.order.usecase.PlaceOrderService;
import parallax.vn.springbootdemo.application.shared.port.out.DomainEventPublisher;
import parallax.vn.springbootdemo.domain.customer.aggregate.Customer;
import parallax.vn.springbootdemo.domain.customer.exception.CustomerNotFoundException;
import parallax.vn.springbootdemo.domain.customer.vo.AccountStatus;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerTier;
import parallax.vn.springbootdemo.domain.customer.vo.Email;
import parallax.vn.springbootdemo.domain.order.aggregate.Order;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;
import parallax.vn.springbootdemo.domain.order.vo.OrderStatus;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecision;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecisionSnapshot;
import parallax.vn.springbootdemo.domain.order.vo.RiskLevel;

class PlaceOrderServiceTest {

    private OrderRepositoryPort orders;
    private CustomerRepositoryPort customers;
    private OrderNumberPort orderNumbers;
    private RiskAssessmentPort risk;
    private OrderDecisionCachePort cache;
    private InventoryPort inventory;
    private DomainEventPublisher events;
    private PlaceOrderService service;

    @BeforeEach
    void setUp() {
        orders = mock(OrderRepositoryPort.class);
        customers = mock(CustomerRepositoryPort.class);
        orderNumbers = mock(OrderNumberPort.class);
        risk = mock(RiskAssessmentPort.class);
        cache = mock(OrderDecisionCachePort.class);
        inventory = mock(InventoryPort.class);
        events = mock(DomainEventPublisher.class);
        service = new PlaceOrderService(orders, customers, orderNumbers, risk, cache, inventory, events);

        when(orderNumbers.next()).thenReturn("ORD-20260101-000001");
        when(orders.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cache.resolveIdempotencyKey(any())).thenReturn(Optional.empty());
    }

    private Customer activeCustomer() {
        return Customer.register(Email.of("a@b.com"), "Alice", null, CustomerTier.STANDARD,
                AccountStatus.ACTIVE);
    }

    private PlaceOrderCommand command() {
        return new PlaceOrderCommand(UUID.randomUUID(), "USD", "CARD",
                new PlaceOrderCommand.Address("1 St", "Hanoi", "VN", "100000"),
                List.of(new PlaceOrderCommand.LineItem("p-1", "Widget", new BigDecimal("3000.00"), 2)),
                null);
    }

    private RiskDecisionSnapshot verdict(RiskDecision d, RiskLevel l, int score) {
        return new RiskDecisionSnapshot(UUID.randomUUID(), score, l, d, List.of(), Instant.now());
    }

    @Test
    void approve_sets_status_and_increments_customer_stats() {
        Customer customer = activeCustomer();
        when(customers.findById(any())).thenReturn(Optional.of(customer));
        when(risk.assess(any())).thenReturn(verdict(RiskDecision.APPROVE, RiskLevel.LOW, 10));

        Order result = service.placeOrder(command());

        assertThat(result.status()).isEqualTo(OrderStatus.APPROVED);
        assertThat(customer.lifetimeOrderCount()).isEqualTo(1);
        verify(customers).save(customer);
        verify(orders, times(2)).save(any(Order.class));
    }

    @Test
    void review_holds_order() {
        when(customers.findById(any())).thenReturn(Optional.of(activeCustomer()));
        when(risk.assess(any())).thenReturn(verdict(RiskDecision.REVIEW, RiskLevel.MEDIUM, 45));

        assertThat(service.placeOrder(command()).status()).isEqualTo(OrderStatus.ON_HOLD);
    }

    @Test
    void reject_releases_inventory_and_skips_stats() {
        Customer customer = activeCustomer();
        when(customers.findById(any())).thenReturn(Optional.of(customer));
        when(risk.assess(any())).thenReturn(verdict(RiskDecision.REJECT, RiskLevel.HIGH, 90));

        Order result = service.placeOrder(command());

        assertThat(result.status()).isEqualTo(OrderStatus.REJECTED);
        assertThat(customer.lifetimeOrderCount()).isZero();
        verify(customers, never()).save(any());
        verify(inventory).release(any());
    }

    @Test
    void unknown_customer_throws() {
        when(customers.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.placeOrder(command()))
                .isInstanceOf(CustomerNotFoundException.class);
        verify(risk, never()).assess(any());
    }

    @Test
    void idempotent_replay_returns_existing_without_reassessing() {
        OrderId existingId = OrderId.newId();
        Order existing = Order.place("ORD-X", parallax.vn.springbootdemo.domain.customer.vo.CustomerId.newId(),
                "USD",
                List.of(parallax.vn.springbootdemo.domain.order.entity.OrderLine.create("p", "n",
                        parallax.vn.springbootdemo.domain.shared.vo.Money.of("1.00", "USD"), 1)),
                null, parallax.vn.springbootdemo.domain.order.vo.PaymentMethod.CARD);
        when(cache.resolveIdempotencyKey("k-1")).thenReturn(Optional.of(existingId));
        when(orders.findById(existingId)).thenReturn(Optional.of(existing));

        PlaceOrderCommand cmd = new PlaceOrderCommand(UUID.randomUUID(), "USD", "CARD", null,
                List.of(new PlaceOrderCommand.LineItem("p-1", "Widget", new BigDecimal("10.00"), 1)), "k-1");

        assertThat(service.placeOrder(cmd)).isSameAs(existing);
        verify(risk, never()).assess(any());
    }

    @Test
    void credit_card_is_accepted_and_mapped_to_card() {
        when(customers.findById(any())).thenReturn(Optional.of(activeCustomer()));
        when(risk.assess(any())).thenReturn(verdict(RiskDecision.APPROVE, RiskLevel.LOW, 10));
        PlaceOrderCommand cmd = new PlaceOrderCommand(UUID.randomUUID(), "USD", "CREDIT_CARD",
                null,
                List.of(new PlaceOrderCommand.LineItem("p-1", "Widget", new BigDecimal("10.00"), 1)),
                null);

        Order result = service.placeOrder(cmd);

        assertThat(result.paymentMethod())
                .isEqualTo(parallax.vn.springbootdemo.domain.order.vo.PaymentMethod.CARD);
        assertThat(result.status()).isEqualTo(OrderStatus.APPROVED);
    }

    @Test
    void unsupported_payment_method_is_rejected_as_validation_error_without_side_effects() {
        when(customers.findById(any())).thenReturn(Optional.of(activeCustomer()));
        PlaceOrderCommand cmd = new PlaceOrderCommand(UUID.randomUUID(), "USD", "BITCOIN",
                null,
                List.of(new PlaceOrderCommand.LineItem("p-1", "Widget", new BigDecimal("10.00"), 1)),
                null);

        assertThatThrownBy(() -> service.placeOrder(cmd))
                .isInstanceOf(parallax.vn.springbootdemo.common.exception.model.ApplicationException.class)
                .hasMessageContaining("BITCOIN")
                .extracting(ex -> ((parallax.vn.springbootdemo.common.exception.model.ApplicationException) ex)
                        .getCode())
                .isEqualTo(parallax.vn.springbootdemo.common.exception.code.ErrorCode.VALIDATION_ERROR);
        verify(orders, never()).save(any());
        verify(inventory, never()).reserve(any(), any());
        verify(risk, never()).assess(any());
    }
}
