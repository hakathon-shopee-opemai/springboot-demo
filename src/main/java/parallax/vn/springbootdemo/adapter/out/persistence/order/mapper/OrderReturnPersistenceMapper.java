package parallax.vn.springbootdemo.adapter.out.persistence.order.mapper;

import org.springframework.stereotype.Component;
import parallax.vn.springbootdemo.adapter.out.persistence.order.entity.OrderReturnEntity;
import parallax.vn.springbootdemo.adapter.out.persistence.order.entity.RefundLedgerEntity;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;
import parallax.vn.springbootdemo.domain.order.aggregate.OrderReturn;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;
import parallax.vn.springbootdemo.domain.order.vo.RefundLedger;
import parallax.vn.springbootdemo.domain.order.vo.ReturnDecision;
import parallax.vn.springbootdemo.domain.order.vo.ReturnReason;
import parallax.vn.springbootdemo.domain.order.vo.ReturnStatus;
import parallax.vn.springbootdemo.domain.shared.vo.Money;

@Component
public class OrderReturnPersistenceMapper {

    public OrderReturnEntity toEntity(OrderReturn r) {
        OrderReturnEntity e = new OrderReturnEntity();
        e.setId(r.id());
        e.setOrderId(r.orderId().value());
        e.setCustomerId(r.customerId().value());
        e.setReasonCode(r.reasonCode().name());
        e.setReason(r.reason());
        e.setRequestedAmount(r.requestedAmount().amount());
        e.setCurrency(r.requestedAmount().currency());
        e.setStatus(r.status().name());
        e.setDecision(r.decision() == null ? null : r.decision().name());
        e.setRiskScore(r.riskScore());
        e.setIdempotencyKey(r.idempotencyKey());
        e.setCreatedAt(r.createdAt());
        e.setUpdatedAt(r.updatedAt());
        e.setVersion(r.version());
        return e;
    }

    public RefundLedgerEntity toRefundEntity(RefundLedger l) {
        RefundLedgerEntity e = new RefundLedgerEntity();
        e.setId(l.id());
        e.setReturnId(l.returnId());
        e.setOrderId(l.orderId());
        e.setAmount(l.amount().amount());
        e.setCurrency(l.amount().currency());
        e.setCreatedAt(l.createdAt());
        return e;
    }

    public OrderReturn toDomain(OrderReturnEntity e, RefundLedgerEntity refund) {
        RefundLedger ledger = refund == null ? null
                : new RefundLedger(refund.getId(), refund.getReturnId(), refund.getOrderId(),
                        Money.of(refund.getAmount(), refund.getCurrency()), refund.getCreatedAt());
        return OrderReturn.rehydrate(
                e.getId(),
                OrderId.of(e.getOrderId()),
                CustomerId.of(e.getCustomerId()),
                ReturnReason.valueOf(e.getReasonCode()),
                e.getReason(),
                Money.of(e.getRequestedAmount(), e.getCurrency()),
                ReturnStatus.valueOf(e.getStatus()),
                e.getDecision() == null ? null : ReturnDecision.valueOf(e.getDecision()),
                e.getRiskScore(),
                ledger,
                e.getIdempotencyKey(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getVersion());
    }
}
