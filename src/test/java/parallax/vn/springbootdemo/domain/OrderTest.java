package parallax.vn.springbootdemo.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;
import parallax.vn.springbootdemo.domain.order.aggregate.Order;
import parallax.vn.springbootdemo.domain.order.entity.OrderLine;
import parallax.vn.springbootdemo.domain.order.exception.IllegalOrderTransitionException;
import parallax.vn.springbootdemo.domain.order.exception.InvalidOrderException;
import parallax.vn.springbootdemo.domain.order.vo.OrderStatus;
import parallax.vn.springbootdemo.domain.order.vo.PaymentMethod;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecision;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecisionSnapshot;
import parallax.vn.springbootdemo.domain.order.vo.RiskLevel;
import parallax.vn.springbootdemo.domain.shared.vo.Money;

class OrderTest {

    private OrderLine line(String price, int qty) {
        return OrderLine.create("p-1", "Widget", Money.of(price, "USD"), qty);
    }

    private Order pendingOrder() {
        return Order.place("ORD-1", CustomerId.newId(), "USD",
                List.of(line("3000.00", 2)), null, PaymentMethod.CARD);
    }

    @Test
    void place_computes_subtotal_and_total() {
        Order order = pendingOrder();
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.total().amount()).isEqualByComparingTo(new BigDecimal("6000.00"));
    }

    @Test
    void place_rejects_empty_lines() {
        assertThatThrownBy(() -> Order.place("ORD-1", CustomerId.newId(), "USD",
                List.of(), null, PaymentMethod.CARD)).isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void applyRisk_approve_moves_to_approved() {
        Order order = pendingOrder();
        order.applyRisk(snapshot(RiskDecision.APPROVE, RiskLevel.LOW, 10));
        assertThat(order.status()).isEqualTo(OrderStatus.APPROVED);
    }

    @Test
    void applyRisk_reject_moves_to_rejected() {
        Order order = pendingOrder();
        order.applyRisk(snapshot(RiskDecision.REJECT, RiskLevel.HIGH, 90));
        assertThat(order.isRejected()).isTrue();
    }

    @Test
    void rejected_is_terminal() {
        Order order = pendingOrder();
        order.applyRisk(snapshot(RiskDecision.REJECT, RiskLevel.HIGH, 90));
        assertThatThrownBy(order::cancel).isInstanceOf(IllegalOrderTransitionException.class);
    }

    private RiskDecisionSnapshot snapshot(RiskDecision d, RiskLevel l, int score) {
        return new RiskDecisionSnapshot(UUID.randomUUID(), score, l, d, List.of(), Instant.now());
    }
}
