package parallax.vn.springbootdemo.adapter.in.web.order.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import parallax.vn.springbootdemo.adapter.in.web.order.dto.request.CancelOrderRequest;
import parallax.vn.springbootdemo.adapter.in.web.order.dto.request.PlaceOrderRequest;
import parallax.vn.springbootdemo.adapter.in.web.order.dto.response.CancellationResponse;
import parallax.vn.springbootdemo.application.order.dto.CancelOrderCommand;
import parallax.vn.springbootdemo.application.order.dto.CancellationResult;
import parallax.vn.springbootdemo.adapter.in.web.order.dto.response.OrderResponse;
import parallax.vn.springbootdemo.application.order.dto.PlaceOrderCommand;
import parallax.vn.springbootdemo.domain.order.aggregate.Order;
import parallax.vn.springbootdemo.domain.order.vo.RiskDecisionSnapshot;

@Component
public class OrderWebMapper {

    public PlaceOrderCommand toCommand(PlaceOrderRequest r, String idempotencyKey) {
        PlaceOrderCommand.Address address = r.shippingAddress() == null ? null
                : new PlaceOrderCommand.Address(r.shippingAddress().line1(), r.shippingAddress().city(),
                        r.shippingAddress().country(), r.shippingAddress().postalCode());
        List<PlaceOrderCommand.LineItem> lines = r.lines().stream()
                .map(l -> new PlaceOrderCommand.LineItem(l.productId(), l.productName(), l.unitPrice(),
                        l.quantity()))
                .toList();
        String idem = idempotencyKey != null ? idempotencyKey : r.idempotencyKey();
        return new PlaceOrderCommand(r.customerId(), r.currency(), r.paymentMethod(), address, lines, idem);
    }

    public OrderResponse toResponse(Order o) {
        OrderResponse.Risk risk = null;
        RiskDecisionSnapshot s = o.risk();
        if (s != null) {
            List<OrderResponse.Factor> factors = s.factors() == null ? List.of()
                    : s.factors().stream()
                            .map(f -> new OrderResponse.Factor(f.code(), f.contribution()))
                            .toList();
            risk = new OrderResponse.Risk(s.score(),
                    s.level() == null ? null : s.level().name(),
                    s.decision() == null ? null : s.decision().name(),
                    factors);
        }
        List<OrderResponse.Line> lines = o.lines().stream()
                .map(l -> new OrderResponse.Line(l.productId(), l.productName(), l.unitPrice().amount(),
                        l.quantity(), l.lineTotal().amount()))
                .toList();
        return new OrderResponse(o.id().value(), o.orderNumber(), o.customerId().value(),
                o.status().name(), o.currency(), o.subtotal().amount(), o.shippingFee().amount(),
                o.total().amount(), risk, lines, o.createdAt());
    }

    public CancelOrderCommand toCancelCommand(java.util.UUID orderId, CancelOrderRequest r,
            String idempotencyKey) {
        return new CancelOrderCommand(orderId, r.reason(), r.requestedBy(), idempotencyKey);
    }

    public CancellationResponse toCancellationResponse(CancellationResult result) {
        List<CancellationResponse.Step> steps = result.steps().stream()
                .map(s -> new CancellationResponse.Step(s.name(), s.status()))
                .toList();
        return new CancellationResponse(result.orderId(), result.status(), result.sagaStatus(),
                result.reason(), steps);
    }
}
