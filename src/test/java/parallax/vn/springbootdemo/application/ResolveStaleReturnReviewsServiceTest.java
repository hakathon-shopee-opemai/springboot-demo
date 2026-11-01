package parallax.vn.springbootdemo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parallax.vn.springbootdemo.application.customer.port.out.CustomerRepositoryPort;
import parallax.vn.springbootdemo.application.order.port.out.OrderDecisionCachePort;
import parallax.vn.springbootdemo.application.order.port.out.OrderRepositoryPort;
import parallax.vn.springbootdemo.application.order.port.out.ReturnRepositoryPort;
import parallax.vn.springbootdemo.application.order.port.out.ReturnRiskAssessmentPort;
import parallax.vn.springbootdemo.application.order.usecase.ResolveStaleReturnReviewsService;
import parallax.vn.springbootdemo.domain.customer.aggregate.Customer;
import parallax.vn.springbootdemo.domain.customer.vo.AccountStatus;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerTier;
import parallax.vn.springbootdemo.domain.customer.vo.Email;
import parallax.vn.springbootdemo.domain.order.aggregate.Order;
import parallax.vn.springbootdemo.domain.order.aggregate.OrderReturn;
import parallax.vn.springbootdemo.domain.order.entity.OrderLine;
import parallax.vn.springbootdemo.domain.order.vo.PaymentMethod;
import parallax.vn.springbootdemo.domain.order.vo.ReturnDecision;
import parallax.vn.springbootdemo.domain.order.vo.ReturnReason;
import parallax.vn.springbootdemo.domain.order.vo.ReturnRiskAssessment;
import parallax.vn.springbootdemo.domain.order.vo.ReturnStatus;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecision;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecisionSnapshot;
import parallax.vn.springbootdemo.domain.order.vo.RiskLevel;
import parallax.vn.springbootdemo.domain.shared.vo.Money;

class ResolveStaleReturnReviewsServiceTest {

    private OrderRepositoryPort orders;
    private ReturnRepositoryPort returns;
    private CustomerRepositoryPort customers;
    private ReturnRiskAssessmentPort riskAssessment;
    private OrderDecisionCachePort cache;
    private ResolveStaleReturnReviewsService service;

    @BeforeEach
    void setUp() {
        orders = mock(OrderRepositoryPort.class);
        returns = mock(ReturnRepositoryPort.class);
        customers = mock(CustomerRepositoryPort.class);
        riskAssessment = mock(ReturnRiskAssessmentPort.class);
        cache = mock(OrderDecisionCachePort.class);
        service = new ResolveStaleReturnReviewsService(returns, orders, customers, riskAssessment,
                cache, 24);
        when(returns.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private Order approvedOrder() {
        Order o = Order.place("ORD-1", CustomerId.newId(), "USD",
                List.of(OrderLine.create("p-1", "Widget", Money.of("100.00", "USD"), 1)),
                null, PaymentMethod.CARD);
        o.applyRisk(new RiskDecisionSnapshot(null, 10, RiskLevel.LOW, RiskDecision.APPROVE,
                List.of(), Instant.now()));
        return o;
    }

    private OrderReturn underReview(Order order) {
        OrderReturn r = OrderReturn.request(order, ReturnReason.DEFECTIVE, "cracked",
                order.total(), 30, null);
        r.applyDecision(ReturnDecision.REVIEW, 45);
        return r;
    }

    private ReturnRiskAssessment verdict(ReturnDecision d) {
        return new ReturnRiskAssessment(UUID.randomUUID(), 20, RiskLevel.LOW, d, List.of(),
                Instant.now());
    }

    private void stub(Order order, OrderReturn... stale) {
        when(returns.findUnderReviewBefore(any())).thenReturn(List.of(stale));
        when(orders.findById(any())).thenReturn(Optional.of(order));
        when(customers.findById(any())).thenReturn(Optional.of(Customer.register(
                Email.of("a@b.com"), "Alice", null, CustomerTier.STANDARD, AccountStatus.ACTIVE)));
    }

    @Test
    void refund_verdict_closes_each_stale_review() {
        Order order = approvedOrder();
        OrderReturn a = underReview(order);
        OrderReturn b = underReview(order);
        stub(order, a, b);
        when(riskAssessment.assess(any())).thenReturn(verdict(ReturnDecision.AUTO_REFUND));

        assertThat(service.resolveStaleReviews()).isEqualTo(2);
        assertThat(a.status()).isEqualTo(ReturnStatus.REFUNDED);
        assertThat(a.refund()).isNotNull();
        verify(returns, times(2)).save(any());
        verify(cache, times(2)).evictCustomerHistory(any());
    }

    @Test
    void review_verdict_keeps_return_open() {
        Order order = approvedOrder();
        OrderReturn a = underReview(order);
        stub(order, a);
        when(riskAssessment.assess(any())).thenReturn(verdict(ReturnDecision.REVIEW));

        assertThat(service.resolveStaleReviews()).isZero();
        assertThat(a.status()).isEqualTo(ReturnStatus.UNDER_REVIEW);
        verify(returns, never()).save(any());
    }

    @Test
    void missing_order_is_skipped_without_calling_risk() {
        Order order = approvedOrder();
        stub(order, underReview(order));
        when(orders.findById(any())).thenReturn(Optional.empty());

        assertThat(service.resolveStaleReviews()).isZero();
        verify(riskAssessment, never()).assess(any());
    }
}
