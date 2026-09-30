package parallax.vn.springbootdemo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parallax.vn.springbootdemo.application.customer.port.out.CustomerRepositoryPort;
import parallax.vn.springbootdemo.application.order.dto.CancelOrderCommand;
import parallax.vn.springbootdemo.application.order.dto.CancellationResult;
import parallax.vn.springbootdemo.application.order.dto.VelocityAdjustResult;
import parallax.vn.springbootdemo.application.order.port.out.DistributedLockPort;
import parallax.vn.springbootdemo.application.order.port.out.InventoryPort;
import parallax.vn.springbootdemo.application.order.port.out.OrderCancellationRepositoryPort;
import parallax.vn.springbootdemo.application.order.port.out.OrderDecisionCachePort;
import parallax.vn.springbootdemo.application.order.port.out.OrderRepositoryPort;
import parallax.vn.springbootdemo.application.order.port.out.RefundPort;
import parallax.vn.springbootdemo.application.order.port.out.RiskAssessmentCancelPort;
import parallax.vn.springbootdemo.application.order.port.out.RiskVelocityPort;
import parallax.vn.springbootdemo.application.order.usecase.CancelOrderSaga;
import parallax.vn.springbootdemo.application.order.usecase.CancellationFinalizer;
import parallax.vn.springbootdemo.common.exception.model.ApplicationException;
import parallax.vn.springbootdemo.domain.customer.aggregate.Customer;
import parallax.vn.springbootdemo.domain.customer.vo.AccountStatus;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerTier;
import parallax.vn.springbootdemo.domain.customer.vo.Email;
import parallax.vn.springbootdemo.domain.order.aggregate.Order;
import parallax.vn.springbootdemo.domain.order.aggregate.OrderCancellation;
import parallax.vn.springbootdemo.domain.order.entity.OrderLine;
import parallax.vn.springbootdemo.domain.order.vo.CancellationStatus;
import parallax.vn.springbootdemo.domain.order.vo.OrderStatus;
import parallax.vn.springbootdemo.domain.order.vo.PaymentMethod;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecision;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecisionSnapshot;
import parallax.vn.springbootdemo.domain.order.vo.RiskLevel;
import parallax.vn.springbootdemo.domain.order.vo.SagaStep;
import parallax.vn.springbootdemo.domain.shared.vo.Money;

class CancelOrderSagaTest {

    private DistributedLockPort lock;
    private OrderRepositoryPort orders;
    private CustomerRepositoryPort customers;
    private OrderCancellationRepositoryPort cancellations;
    private InventoryPort inventory;
    private RiskVelocityPort velocity;
    private RiskAssessmentCancelPort assessmentCancel;
    private RefundPort refund;
    private OrderDecisionCachePort cache;
    private CancellationFinalizer finalizer;
    private CancelOrderSaga saga;

    @BeforeEach
    void setUp() {
        lock = mock(DistributedLockPort.class);
        orders = mock(OrderRepositoryPort.class);
        customers = mock(CustomerRepositoryPort.class);
        cancellations = mock(OrderCancellationRepositoryPort.class);
        inventory = mock(InventoryPort.class);
        velocity = mock(RiskVelocityPort.class);
        assessmentCancel = mock(RiskAssessmentCancelPort.class);
        refund = mock(RefundPort.class);
        cache = mock(OrderDecisionCachePort.class);
        finalizer = mock(CancellationFinalizer.class);
        saga = new CancelOrderSaga(lock, orders, customers, cancellations, inventory, velocity,
                assessmentCancel, refund, cache, finalizer);

        when(lock.tryLock(any(), any(Duration.class))).thenReturn(Optional.of("token"));
        when(cancellations.save(any())).thenAnswer(i -> i.getArgument(0));
        when(velocity.adjust(any(), anyInt(), any()))
                .thenReturn(new VelocityAdjustResult(1, 0, true));
        when(finalizer.finalizeSuccess(any(), any(), any())).thenAnswer(i -> {
            OrderCancellation c = i.getArgument(2);
            c.complete();
            return c;
        });
    }

    private Order approvedOrder() {
        Order o = Order.place("ORD-1", parallax.vn.springbootdemo.domain.customer.vo.CustomerId.newId(),
                "USD",
                List.of(OrderLine.create("p-1", "Widget", Money.of("100.00", "USD"), 1)),
                null, PaymentMethod.CARD);
        o.applyRisk(new RiskDecisionSnapshot(UUID.randomUUID(), 10, RiskLevel.LOW, RiskDecision.APPROVE,
                List.of(), Instant.now()));
        return o;
    }

    private Customer countedCustomer() {
        Customer c = Customer.register(Email.of("a@b.com"), "Alice", null, CustomerTier.STANDARD,
                AccountStatus.ACTIVE);
        c.registerPlacedOrder(Money.of("100.00", "USD"), Instant.now());
        return c;
    }

    private CancelOrderCommand cmd(UUID orderId) {
        return new CancelOrderCommand(orderId, "customer changed mind", "ops", null);
    }

    @Test
    void happy_path_completes_reverses_stats_and_evicts_caches() {
        Order order = approvedOrder();
        Customer customer = countedCustomer();
        when(cancellations.findByOrderId(any())).thenReturn(Optional.empty());
        when(orders.findById(any())).thenReturn(Optional.of(order));
        when(customers.findById(any())).thenReturn(Optional.of(customer));

        CancellationResult result = saga.cancelOrder(cmd(order.id().value()));

        assertThat(result.status()).isEqualTo("CANCELLED");
        assertThat(result.sagaStatus()).isEqualTo("COMPLETED");
        assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(customer.lifetimeOrderCount()).isZero();
        verify(inventory).release(any());
        verify(velocity).adjust(eq(order.customerId().value()), eq(-1), any());
        verify(refund).refund(any());
        verify(finalizer).finalizeSuccess(any(), any(), any());
        verify(cache).evictDecision(any());
        verify(cache).evictCustomerHistory(any());
        verify(lock).unlock(any(), eq("token"));
    }

    @Test
    void velocity_failure_compensates_step1_and_does_not_cancel() {
        Order order = approvedOrder();
        when(cancellations.findByOrderId(any())).thenReturn(Optional.empty());
        when(orders.findById(any())).thenReturn(Optional.of(order));
        when(customers.findById(any())).thenReturn(Optional.of(countedCustomer()));
        when(velocity.adjust(any(), eq(-1), any())).thenThrow(new RuntimeException("risk down"));

        assertThatThrownBy(() -> saga.cancelOrder(cmd(order.id().value())))
                .isInstanceOf(ApplicationException.class);

        verify(inventory).release(any());
        verify(inventory).reserveLine(any(), any()); // S1 compensation ran
        verify(finalizer, never()).finalizeSuccess(any(), any(), any());
        assertThat(order.status()).isEqualTo(OrderStatus.APPROVED); // not cancelled
        verify(lock).unlock(any(), eq("token"));
    }

    @Test
    void not_cancellable_state_is_rejected_without_side_effects() {
        Order rejected = Order.place("ORD-2",
                parallax.vn.springbootdemo.domain.customer.vo.CustomerId.newId(), "USD",
                List.of(OrderLine.create("p-1", "Widget", Money.of("100.00", "USD"), 1)), null,
                PaymentMethod.CARD);
        rejected.applyRisk(new RiskDecisionSnapshot(UUID.randomUUID(), 90, RiskLevel.HIGH,
                RiskDecision.REJECT, List.of(), Instant.now()));
        when(cancellations.findByOrderId(any())).thenReturn(Optional.empty());
        when(orders.findById(any())).thenReturn(Optional.of(rejected));

        assertThatThrownBy(() -> saga.cancelOrder(cmd(rejected.id().value())))
                .isInstanceOf(ApplicationException.class);

        verify(inventory, never()).release(any());
        verify(velocity, never()).adjust(any(), anyInt(), any());
        verify(cancellations, never()).save(any());
        verify(lock).unlock(any(), eq("token"));
    }

    @Test
    void idempotent_replay_returns_existing_completed_without_rerunning() {
        Order order = approvedOrder();
        OrderCancellation done = OrderCancellation.rehydrate(UUID.randomUUID(), order.id(),
                CancellationStatus.COMPLETED, "r", "ops",
                List.of(SagaStep.done("releaseInventory"), SagaStep.done("velocityAdjust"),
                        SagaStep.done("refund")),
                null, Instant.now(), Instant.now(), 1L);
        when(cancellations.findByOrderId(any())).thenReturn(Optional.of(done));
        when(orders.findById(any())).thenReturn(Optional.of(order));

        CancellationResult result = saga.cancelOrder(cmd(order.id().value()));

        assertThat(result.sagaStatus()).isEqualTo("COMPLETED");
        verify(inventory, never()).release(any());
        verify(velocity, never()).adjust(any(), anyInt(), any());
        verify(finalizer, never()).finalizeSuccess(any(), any(), any());
        verify(lock).unlock(any(), eq("token"));
    }
}
