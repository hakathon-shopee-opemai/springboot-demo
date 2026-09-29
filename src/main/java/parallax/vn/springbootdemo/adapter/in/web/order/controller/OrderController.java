package parallax.vn.springbootdemo.adapter.in.web.order.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import parallax.vn.springbootdemo.adapter.in.web.order.dto.request.CancelOrderRequest;
import parallax.vn.springbootdemo.adapter.in.web.order.dto.request.PlaceOrderRequest;
import parallax.vn.springbootdemo.adapter.in.web.order.dto.response.CancellationResponse;
import parallax.vn.springbootdemo.adapter.in.web.order.dto.response.OrderResponse;
import parallax.vn.springbootdemo.adapter.in.web.order.mapper.OrderWebMapper;
import parallax.vn.springbootdemo.application.order.port.in.CancelOrderUseCase;
import parallax.vn.springbootdemo.application.order.port.in.GetOrderUseCase;
import parallax.vn.springbootdemo.application.order.port.in.PlaceOrderUseCase;
import parallax.vn.springbootdemo.common.annotation.WebAdapter;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;

@WebAdapter
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final PlaceOrderUseCase placeOrder;
    private final GetOrderUseCase getOrder;
    private final CancelOrderUseCase cancelOrder;
    private final OrderWebMapper mapper;

    public OrderController(PlaceOrderUseCase placeOrder, GetOrderUseCase getOrder,
            CancelOrderUseCase cancelOrder, OrderWebMapper mapper) {
        this.placeOrder = placeOrder;
        this.getOrder = getOrder;
        this.cancelOrder = cancelOrder;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> place(@Valid @RequestBody PlaceOrderRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        var order = placeOrder.placeOrder(mapper.toCommand(request, idempotencyKey));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(order));
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable UUID id) {
        return mapper.toResponse(getOrder.getOrder(OrderId.of(id)));
    }

    @PostMapping("/{id}/cancel")
    public CancellationResponse cancel(@PathVariable UUID id,
            @Valid @RequestBody CancelOrderRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        var result = cancelOrder.cancelOrder(mapper.toCancelCommand(id, request, idempotencyKey));
        return mapper.toCancellationResponse(result);
    }
}
