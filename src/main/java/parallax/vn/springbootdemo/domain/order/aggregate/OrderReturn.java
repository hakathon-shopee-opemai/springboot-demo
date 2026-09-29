package parallax.vn.springbootdemo.domain.order.aggregate;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;
import parallax.vn.springbootdemo.domain.order.exception.ReturnNotAllowedException;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;
import parallax.vn.springbootdemo.domain.order.vo.OrderStatus;
import parallax.vn.springbootdemo.domain.order.vo.RefundLedger;
import parallax.vn.springbootdemo.domain.order.vo.ReturnDecision;
import parallax.vn.springbootdemo.domain.order.vo.ReturnReason;
import parallax.vn.springbootdemo.domain.order.vo.ReturnStatus;
import parallax.vn.springbootdemo.domain.shared.vo.Money;

/** Return-request aggregate: enforces returnable-state, window and amount invariants and
 * carries the resolved decision plus (when refunded) its refund ledger entry. */
public class OrderReturn {

    private final UUID id;
    private final OrderId orderId;
    private final CustomerId customerId;
    private final ReturnReason reasonCode;
    private final String reason;
    private final Money requestedAmount;
    private ReturnStatus status;
    private ReturnDecision decision;
    private int riskScore;
    private RefundLedger refund;
    private final String idempotencyKey;
    private final Instant createdAt;
    private Instant updatedAt;
    private long version;

    private OrderReturn(UUID id, OrderId orderId, CustomerId customerId, ReturnReason reasonCode,
            String reason, Money requestedAmount, ReturnStatus status, ReturnDecision decision,
            int riskScore, RefundLedger refund, String idempotencyKey, Instant createdAt,
            Instant updatedAt, long version) {
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.reasonCode = reasonCode;
        this.reason = reason;
        this.requestedAmount = requestedAmount;
        this.status = status;
        this.decision = decision;
        this.riskScore = riskScore;
        this.refund = refund;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    /** Factory enforcing the return invariants against the source order. */
    public static OrderReturn request(Order order, ReturnReason reasonCode, String reason,
            Money requestedAmount, int windowDays, String idempotencyKey) {
        if (order.status() != OrderStatus.APPROVED) {
            throw new ReturnNotAllowedException("order is not in a returnable state: " + order.status());
        }
        Instant deadline = order.createdAt().plus(Duration.ofDays(windowDays));
        if (Instant.now().isAfter(deadline)) {
            throw new ReturnNotAllowedException("return window of " + windowDays + " days has expired");
        }
        if (!requestedAmount.currency().equals(order.currency())) {
            throw new ReturnNotAllowedException("requested amount currency must match the order currency");
        }
        if (requestedAmount.amount().signum() <= 0) {
            throw new ReturnNotAllowedException("requested amount must be positive");
        }
        if (requestedAmount.amount().compareTo(order.total().amount()) > 0) {
            throw new ReturnNotAllowedException("requested amount exceeds the order total");
        }
        Instant now = Instant.now();
        return new OrderReturn(UUID.randomUUID(), order.id(), order.customerId(), reasonCode, reason,
                requestedAmount, ReturnStatus.REQUESTED, null, 0, null, idempotencyKey, now, now, 0L);
    }

    public static OrderReturn rehydrate(UUID id, OrderId orderId, CustomerId customerId,
            ReturnReason reasonCode, String reason, Money requestedAmount, ReturnStatus status,
            ReturnDecision decision, int riskScore, RefundLedger refund, String idempotencyKey,
            Instant createdAt, Instant updatedAt, long version) {
        return new OrderReturn(id, orderId, customerId, reasonCode, reason, requestedAmount, status,
                decision, riskScore, refund, idempotencyKey, createdAt, updatedAt, version);
    }

    /** Apply the risk verdict; a REFUNDED outcome mints the (single) refund ledger entry. */
    public void applyDecision(ReturnDecision decision, int riskScore) {
        transitionTo(decision.toReturnStatus());
        this.decision = decision;
        this.riskScore = riskScore;
        if (this.status == ReturnStatus.REFUNDED && this.refund == null) {
            this.refund = RefundLedger.create(this.id, this.orderId.value(), this.requestedAmount);
        }
        this.updatedAt = Instant.now();
    }

    private void transitionTo(ReturnStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new ReturnNotAllowedException("cannot transition return from " + status + " to " + target);
        }
        this.status = target;
    }

    public UUID id() {
        return id;
    }

    public OrderId orderId() {
        return orderId;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public ReturnReason reasonCode() {
        return reasonCode;
    }

    public String reason() {
        return reason;
    }

    public Money requestedAmount() {
        return requestedAmount;
    }

    public ReturnStatus status() {
        return status;
    }

    public ReturnDecision decision() {
        return decision;
    }

    public int riskScore() {
        return riskScore;
    }

    public RefundLedger refund() {
        return refund;
    }

    public String idempotencyKey() {
        return idempotencyKey;
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
