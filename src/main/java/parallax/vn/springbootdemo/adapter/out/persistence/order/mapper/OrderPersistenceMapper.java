package parallax.vn.springbootdemo.adapter.out.persistence.order.mapper;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import parallax.vn.springbootdemo.adapter.out.persistence.order.entity.OrderEntity;
import parallax.vn.springbootdemo.adapter.out.persistence.order.entity.OrderLineEntity;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;
import parallax.vn.springbootdemo.domain.order.aggregate.Order;
import parallax.vn.springbootdemo.domain.order.entity.OrderLine;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;
import parallax.vn.springbootdemo.domain.order.vo.OrderLineId;
import parallax.vn.springbootdemo.domain.order.vo.OrderStatus;
import parallax.vn.springbootdemo.domain.order.vo.PaymentMethod;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecision;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecisionSnapshot;
import parallax.vn.springbootdemo.domain.order.vo.RiskLevel;
import parallax.vn.springbootdemo.domain.shared.vo.Address;
import parallax.vn.springbootdemo.domain.shared.vo.Money;

@Component
public class OrderPersistenceMapper {

    public OrderEntity toEntity(Order o) {
        OrderEntity e = new OrderEntity();
        e.setId(o.id().value());
        e.setOrderNumber(o.orderNumber());
        e.setCustomerId(o.customerId().value());
        e.setStatus(o.status().name());
        e.setCurrency(o.currency());
        e.setSubtotalAmount(o.subtotal().amount());
        e.setShippingFeeAmount(o.shippingFee().amount());
        e.setTotalAmount(o.total().amount());
        if (o.shippingAddress() != null) {
            e.setShipLine1(o.shippingAddress().line1());
            e.setShipCity(o.shippingAddress().city());
            e.setShipCountry(o.shippingAddress().country());
            e.setShipPostalCode(o.shippingAddress().postalCode());
        }
        e.setPaymentMethod(o.paymentMethod().name());
        RiskDecisionSnapshot risk = o.risk();
        if (risk != null) {
            e.setRiskScore(risk.score());
            e.setRiskLevel(risk.level() == null ? null : risk.level().name());
            e.setRiskDecision(risk.decision() == null ? null : risk.decision().name());
            e.setRiskAssessmentId(risk.assessmentId());
            e.setRiskAssessedAt(risk.assessedAt());
        }
        e.setCreatedAt(o.createdAt());
        e.setUpdatedAt(o.updatedAt());
        e.setVersion(o.version());
        for (OrderLine line : o.lines()) {
            OrderLineEntity le = new OrderLineEntity();
            le.setId(line.id().value());
            le.setProductId(line.productId());
            le.setProductName(line.productName());
            le.setUnitPriceAmount(line.unitPrice().amount());
            le.setQuantity(line.quantity());
            le.setLineTotalAmount(line.lineTotal().amount());
            e.addLine(le);
        }
        return e;
    }

    public Order toDomain(OrderEntity e) {
        String currency = e.getCurrency();
        List<OrderLine> lines = new ArrayList<>();
        for (OrderLineEntity le : e.getLines()) {
            lines.add(OrderLine.rehydrate(
                    OrderLineId.of(le.getId()),
                    le.getProductId(),
                    le.getProductName(),
                    Money.of(le.getUnitPriceAmount(), currency),
                    le.getQuantity(),
                    Money.of(le.getLineTotalAmount(), currency)));
        }
        Address address = null;
        if (e.getShipLine1() != null || e.getShipCity() != null) {
            address = new Address(e.getShipLine1(), e.getShipCity(), e.getShipCountry(),
                    e.getShipPostalCode());
        }
        RiskDecisionSnapshot risk = null;
        if (e.getRiskDecision() != null) {
            risk = new RiskDecisionSnapshot(
                    e.getRiskAssessmentId(),
                    e.getRiskScore(),
                    e.getRiskLevel() == null ? null : RiskLevel.valueOf(e.getRiskLevel()),
                    RiskDecision.valueOf(e.getRiskDecision()),
                    List.of(),
                    e.getRiskAssessedAt());
        }
        return Order.rehydrate(
                OrderId.of(e.getId()),
                e.getOrderNumber(),
                CustomerId.of(e.getCustomerId()),
                OrderStatus.valueOf(e.getStatus()),
                currency,
                lines,
                Money.of(e.getSubtotalAmount(), currency),
                Money.of(e.getShippingFeeAmount(), currency),
                Money.of(e.getTotalAmount(), currency),
                address,
                PaymentMethod.valueOf(e.getPaymentMethod()),
                risk,
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getVersion());
    }
}
