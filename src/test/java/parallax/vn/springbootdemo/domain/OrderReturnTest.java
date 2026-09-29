package parallax.vn.springbootdemo.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;
import parallax.vn.springbootdemo.domain.order.aggregate.Order;
import parallax.vn.springbootdemo.domain.order.aggregate.OrderReturn;
import parallax.vn.springbootdemo.domain.order.entity.OrderLine;
import parallax.vn.springbootdemo.domain.order.exception.ReturnNotAllowedException;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;
import parallax.vn.springbootdemo.domain.order.vo.OrderStatus;
import parallax.vn.springbootdemo.domain.order.vo.PaymentMethod;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecision;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecisionSnapshot;
import parallax.vn.springbootdemo.domain.order.vo.RiskLevel;
import parallax.vn.springbootdemo.domain.order.vo.ReturnDecision;
import parallax.vn.springbootdemo.domain.order.vo.ReturnReason;
import parallax.vn.springbootdemo.domain.order.vo.ReturnStatus;
import parallax.vn.springbootdemo.domain.shared.vo.Money;

class OrderReturnTest {

    private static final int WINDOW_DAYS = 30;

    private Order approvedOrder() {
        Order o = Order.place("ORD-1", CustomerId.newId(), "USD",
                List.of(OrderLine.create("p-1", "Widget", Money.of("100.00", "USD"), 1)),
                null, PaymentMethod.CARD);
        o.applyRisk(new RiskDecisionSnapshot(null, 10, RiskLevel.LOW, RiskDecision.APPROVE,
                List.of(), Instant.now()));
        return o;
    }

    /** APPROVED order whose createdAt is {@code daysAgo} in the past. */
    private Order approvedOrderCreated(long daysAgo) {
        Instant created = Instant.now().minus(daysAgo, ChronoUnit.DAYS);
        return Order.rehydrate(OrderId.newId(), "ORD-OLD", CustomerId.newId(), OrderStatus.APPROVED,
                "USD", List.of(OrderLine.create("p-1", "Widget", Money.of("100.00", "USD"), 1)),
                Money.of("100.00", "USD"), Money.zero("USD"), Money.of("100.00", "USD"), null,
                PaymentMethod.CARD, null, created, created, 0L);
    }

    private OrderReturn request(Order order, String amount) {
        return OrderReturn.request(order, ReturnReason.DEFECTIVE, "screen cracked",
                Money.of(amount, "USD"), WINDOW_DAYS, null);
    }

    @Test
    void request_on_approved_within_window_starts_requested() {
        OrderReturn r = request(approvedOrder(), "100.00");
        assertThat(r.status()).isEqualTo(ReturnStatus.REQUESTED);
        assertThat(r.reasonCode()).isEqualTo(ReturnReason.DEFECTIVE);
        assertThat(r.refund()).isNull();
    }

    @Test
    void request_rejects_non_returnable_state() {
        Order pending = Order.place("ORD-2", CustomerId.newId(), "USD",
                List.of(OrderLine.create("p-1", "Widget", Money.of("100.00", "USD"), 1)), null,
                PaymentMethod.CARD);
        assertThatThrownBy(() -> request(pending, "100.00"))
                .isInstanceOf(ReturnNotAllowedException.class);
    }

    @Test
    void request_rejects_when_window_expired() {
        assertThatThrownBy(() -> request(approvedOrderCreated(40), "100.00"))
                .isInstanceOf(ReturnNotAllowedException.class)
                .hasMessageContaining("window");
    }

    @Test
    void request_allows_return_on_last_day_of_window() {
        OrderReturn r = request(approvedOrderCreated(10), "50.00");
        assertThat(r.status()).isEqualTo(ReturnStatus.REQUESTED);
    }

    @Test
    void request_rejects_amount_over_order_total() {
        assertThatThrownBy(() -> request(approvedOrder(), "150.00"))
                .isInstanceOf(ReturnNotAllowedException.class)
                .hasMessageContaining("exceeds");
    }

    @Test
    void request_rejects_non_positive_amount() {
        assertThatThrownBy(() -> request(approvedOrder(), "0.00"))
                .isInstanceOf(ReturnNotAllowedException.class);
    }

    @Test
    void apply_auto_refund_marks_refunded_and_mints_ledger() {
        OrderReturn r = request(approvedOrder(), "100.00");
        r.applyDecision(ReturnDecision.AUTO_REFUND, 15);
        assertThat(r.status()).isEqualTo(ReturnStatus.REFUNDED);
        assertThat(r.decision()).isEqualTo(ReturnDecision.AUTO_REFUND);
        assertThat(r.riskScore()).isEqualTo(15);
        assertThat(r.refund()).isNotNull();
        assertThat(r.refund().amount().amount()).isEqualByComparingTo("100.00");
        assertThat(r.refund().returnId()).isEqualTo(r.id());
    }

    @Test
    void apply_review_marks_under_review_without_ledger() {
        OrderReturn r = request(approvedOrder(), "100.00");
        r.applyDecision(ReturnDecision.REVIEW, 45);
        assertThat(r.status()).isEqualTo(ReturnStatus.UNDER_REVIEW);
        assertThat(r.refund()).isNull();
    }

    @Test
    void apply_deny_marks_denied_without_ledger() {
        OrderReturn r = request(approvedOrder(), "100.00");
        r.applyDecision(ReturnDecision.DENY, 90);
        assertThat(r.status()).isEqualTo(ReturnStatus.DENIED);
        assertThat(r.refund()).isNull();
    }

    @Test
    void apply_decision_twice_is_illegal_transition() {
        OrderReturn r = request(approvedOrder(), "100.00");
        r.applyDecision(ReturnDecision.REVIEW, 45);
        assertThatThrownBy(() -> r.applyDecision(ReturnDecision.AUTO_REFUND, 10))
                .isInstanceOf(ReturnNotAllowedException.class);
    }

    @Test
    void status_active_flag_matches_index_predicate() {
        assertThat(ReturnStatus.REFUNDED.isActive()).isTrue();
        assertThat(ReturnStatus.UNDER_REVIEW.isActive()).isTrue();
        assertThat(ReturnStatus.DENIED.isActive()).isFalse();
        assertThat(ReturnStatus.REQUESTED.canTransitionTo(ReturnStatus.REFUNDED)).isTrue();
        assertThat(ReturnStatus.DENIED.canTransitionTo(ReturnStatus.REFUNDED)).isFalse();
    }
}
