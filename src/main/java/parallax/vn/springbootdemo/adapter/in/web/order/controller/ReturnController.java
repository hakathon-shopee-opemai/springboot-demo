package parallax.vn.springbootdemo.adapter.in.web.order.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import parallax.vn.springbootdemo.adapter.in.web.order.dto.request.CreateReturnRequest;
import parallax.vn.springbootdemo.adapter.in.web.order.dto.response.ReturnResponse;
import parallax.vn.springbootdemo.adapter.in.web.order.mapper.ReturnWebMapper;
import parallax.vn.springbootdemo.application.order.port.in.RequestReturnUseCase;
import parallax.vn.springbootdemo.common.annotation.WebAdapter;

@WebAdapter
@RestController
@RequestMapping("/api/v1/orders")
public class ReturnController {

    private final RequestReturnUseCase requestReturn;
    private final ReturnWebMapper mapper;

    public ReturnController(RequestReturnUseCase requestReturn, ReturnWebMapper mapper) {
        this.requestReturn = requestReturn;
        this.mapper = mapper;
    }

    @PostMapping("/{orderId}/returns")
    public ResponseEntity<ReturnResponse> requestReturn(@PathVariable UUID orderId,
            @Valid @RequestBody CreateReturnRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        var result = requestReturn.requestReturn(mapper.toCommand(orderId, request, idempotencyKey));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(result));
    }
}
