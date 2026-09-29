package parallax.vn.springbootdemo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import parallax.vn.springbootdemo.application.order.dto.RequestReturnCommand;
import parallax.vn.springbootdemo.application.order.dto.ReturnResult;
import parallax.vn.springbootdemo.application.order.port.out.OrderDecisionCachePort;
import parallax.vn.springbootdemo.application.order.port.out.OrderRepositoryPort;
import parallax.vn.springbootdemo.application.order.port.out.ReturnRepositoryPort;
import parallax.vn.springbootdemo.application.order.port.out.ReturnRiskAssessmentPort;
import parallax.vn.springbootdemo.application.order.usecase.RequestReturnService;
import parallax.vn.springbootdemo.common.exception.code.ErrorCode;
import parallax.vn.springbootdemo.common.exception.model.ApplicationException;
import parallax.vn.springbootdemo.domain.customer.aggregate.Customer;
import parallax.vn.springbootdemo.domain.customer.vo.AccountStatus;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerTier;
import parallax.vn.springbootdemo.domain.customer.vo.Email;
import parallax.vn.springbootdemo.domain.order.aggregate.Order;
import parallax.vn.springbootdemo.domain.order.aggregate.OrderReturn;
import parallax.vn.springbootdemo.domain.order.entity.OrderLine;
import parallax.vn.springbootdemo.domain.order.exception.ReturnNotAllowedException;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecision;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecisionSnapshot;
import parallax.vn.springbootdemo.domain.order.vo.RiskLevel;
import parallax.vn.springbootdemo.domain.order.vo.ReturnDecision;
import parallax.vn.springbootdemo.domain.order.vo.ReturnReason;
import parallax.vn.springbootdemo.domain.order.vo.ReturnRiskAssessment;
import parallax.vn.springbootdemo.domain.shared.vo.Money;

class RequestReturnServiceTest {

    private OrderRepositoryPort orders;
    private ReturnRepositoryPort returns;
    private CustomerRepositoryPort customers;
    private ReturnRiskAssessmentPort riskAssessment;
    private OrderDecisionCachePort cache;
    private RequestReturnService service;

    @BeforeEach
    void setUp() {
        orders = mock(OrderRepositoryPort.class);
        returns = mock(ReturnRepositoryPort.class);
        customers = mock(CustomerRepositoryPort.class);
        riskAssessment = mock(ReturnRiskAssessmentPort.class);
        cache = mock(OrderDecisionCachePort.class);
        service = new RequestReturnService(orders, returns, customers, riskAssessment, cache, 30);

        when(returns.save(any())).thenAnswer(i -> i.getArgument(0));
        when(returns.existsActiveReturn(any())).thenReturn(false);
        when(cache.resolveReturnIdempotencyKey(any())).thenReturn(Optional.empty());
    }

    private Order approvedOrder() {
        Order o = Order.place("ORD-1", CustomerId.newId(), "USD",
                List.of(OrderLine.create("p-1", "Widget", Money.of("100.00", "USD"), 1)),
                null, PaymentMethod());
        o.applyRisk(new RiskDecisionSnapshot(null, 10, RiskLevel.LOW, RiskDecision.APPROVE,
                List.of(), Instant.now()));
        return o;
    }

    private static parallax.vn.springbootdemo.domain.order.vo.PaymentMethod PaymentMethod() {
        return parallax.vn.springbootdemo.domain.order.vo.PaymentMethod.CARD;
    }

    private Customer activeCustomer() {
        return Customer.register(Email.of("a@b.com"), "Alice", null, CustomerTier.STANDARD,
                AccountStatus.ACTIVE);
    }

    private ReturnRiskAssessment verdict(ReturnDecision d, RiskLevel l, int score) {
        return new ReturnRiskAssessment(UUID.randomUUID(), score, l, d, List.of(), Instant.now());
    }

    private RequestReturnCommand cmd(UUID orderId, BigDecimal amount) {
        return new RequestReturnCommand(orderId, "DEFECTIVE", "screen cracked", amount, null);
    }

    private void stubOrderAndCustomer(Order order) {
        when(orders.findById(any())).thenReturn(Optional.of(order));
        when(customers.findById(any())).thenReturn(Optional.of(activeCustomer()));
    }

    @Test
    void auto_refund_marks_refunded_and_returns_refund_details() {
        Order order = approvedOrder();
        stubOrderAndCustomer(order);
        when(riskAssessment.assess(any())).thenReturn(verdict(ReturnDecision.AUTO_REFUND, RiskLevel.LOW, 15));

        ReturnResult result = service.requestReturn(cmd(order.id().value(), null));

        assertThat(result.status()).isEqualTo("REFUNDED");
        assertThat(result.decision()).isEqualTo("AUTO_REFUND");
        assertThat(result.riskScore()).isEqualTo(15);
        assertThat(result.refund()).isNotNull();
        assertThat(result.refund().amount()).isEqualByComparingTo("100.00");
        assertThat(result.refund().currency()).isEqualTo("USD");
        assertThat(result.refund().ledgerId()).isNotNull();
        verify(returns).save(any());
        verify(cache).incrementReturnVelocity(any());
    }

    @Test
    void review_holds_return_under_review_without_refund() {
        Order order = approvedOrder();
        stubOrderAndCustomer(order);
        when(riskAssessment.assess(any())).thenReturn(verdict(ReturnDecision.REVIEW, RiskLevel.MEDIUM, 45));

        ReturnResult result = service.requestReturn(cmd(order.id().value(), null));

        assertThat(result.status()).isEqualTo("UNDER_REVIEW");
        assertThat(result.refund()).isNull();
    }

    @Test
    void deny_marks_denied_without_refund() {
        Order order = approvedOrder();
        stubOrderAndCustomer(order);
        when(riskAssessment.assess(any())).thenReturn(verdict(ReturnDecision.DENY, RiskLevel.HIGH, 90));

        ReturnResult result = service.requestReturn(cmd(order.id().value(), null));

        assertThat(result.status()).isEqualTo("DENIED");
        assertThat(result.refund()).isNull();
    }

    @Test
    void fail_safe_review_verdict_holds_return_under_review() {
        Order order = approvedOrder();
        stubOrderAndCustomer(order);
        // simulates the adapter's fail-safe fallback when the risk hop fails
        when(riskAssessment.assess(any())).thenReturn(new ReturnRiskAssessment(null, 50,
                RiskLevel.MEDIUM, ReturnDecision.REVIEW, List.of(), Instant.now()));

        ReturnResult result = service.requestReturn(cmd(order.id().value(), null));

        assertThat(result.status()).isEqualTo("UNDER_REVIEW");
        assertThat(result.refund()).isNull();
    }

    @Test
    void idempotent_replay_returns_existing_without_reassessing() {
        Order order = approvedOrder();
        OrderReturn existing = OrderReturn.request(order, ReturnReason.DEFECTIVE, "x",
                Money.of("100.00", "USD"), 30, "k-1");
        existing.applyDecision(ReturnDecision.AUTO_REFUND, 15);
        when(cache.resolveReturnIdempotencyKey("k-1")).thenReturn(Optional.of(existing.id()));
        when(returns.findById(existing.id())).thenReturn(Optional.of(existing));

        ReturnResult result = service.requestReturn(
                new RequestReturnCommand(order.id().value(), "DEFECTIVE", "x", null, "k-1"));

        assertThat(result.returnId()).isEqualTo(existing.id());
        assertThat(result.status()).isEqualTo("REFUNDED");
        verify(riskAssessment, never()).assess(any());
        verify(returns, never()).save(any());
    }

    @Test
    void not_returnable_order_throws_and_skips_hop() {
        Order pending = Order.place("ORD-P", CustomerId.newId(), "USD",
                List.of(OrderLine.create("p-1", "Widget", Money.of("100.00", "USD"), 1)), null,
                PaymentMethod());
        stubOrderAndCustomer(pending);

        assertThatThrownBy(() -> service.requestReturn(cmd(pending.id().value(), null)))
                .isInstanceOf(ReturnNotAllowedException.class);
        verify(riskAssessment, never()).assess(any());
        verify(returns, never()).save(any());
    }

    @Test
    void amount_over_total_throws() {
        Order order = approvedOrder();
        stubOrderAndCustomer(order);

        assertThatThrownBy(() -> service.requestReturn(cmd(order.id().value(), new BigDecimal("150.00"))))
                .isInstanceOf(ReturnNotAllowedException.class);
        verify(riskAssessment, never()).assess(any());
    }

    @Test
    void double_return_is_conflict() {
        Order order = approvedOrder();
        stubOrderAndCustomer(order);
        when(returns.existsActiveReturn(any())).thenReturn(true);

        assertThatThrownBy(() -> service.requestReturn(cmd(order.id().value(), null)))
                .isInstanceOf(ApplicationException.class)
                .satisfies(e -> assertThat(((ApplicationException) e).getCode()).isEqualTo(ErrorCode.CONFLICT));
        verify(riskAssessment, never()).assess(any());
    }

    @Test
    void unknown_order_is_not_found() {
        when(orders.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requestReturn(cmd(UUID.randomUUID(), null)))
                .isInstanceOf(ApplicationException.class)
                .satisfies(e -> assertThat(((ApplicationException) e).getCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    void unknown_reason_code_is_validation_error() {
        Order order = approvedOrder();
        stubOrderAndCustomer(order);

        assertThatThrownBy(() -> service.requestReturn(
                new RequestReturnCommand(order.id().value(), "BOGUS", null, null, null)))
                .isInstanceOf(ApplicationException.class)
                .satisfies(e -> assertThat(((ApplicationException) e).getCode())
                        .isEqualTo(ErrorCode.VALIDATION_ERROR));
    }
}
