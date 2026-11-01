package parallax.vn.springbootdemo.adapter.in.web.order.mapper;

import java.util.UUID;
import org.springframework.stereotype.Component;
import parallax.vn.springbootdemo.adapter.in.web.order.dto.request.CreateReturnRequest;
import parallax.vn.springbootdemo.adapter.in.web.order.dto.response.ReturnResponse;
import parallax.vn.springbootdemo.application.order.dto.RequestReturnCommand;
import parallax.vn.springbootdemo.application.order.dto.ReturnResult;

@Component
public class ReturnWebMapper {

    public RequestReturnCommand toCommand(UUID orderId, CreateReturnRequest r, String idempotencyKey) {
        return new RequestReturnCommand(orderId, r.reasonCode(), r.reason(), r.requestedAmount(),
                idempotencyKey);
    }

    public ReturnResponse toResponse(ReturnResult result) {
        ReturnResponse.Refund refund = result.refund() == null ? null
                : new ReturnResponse.Refund(result.refund().amount(), result.refund().currency(),
                        result.refund().ledgerId());
        return new ReturnResponse(result.returnId(), result.orderId(), result.status(),
                result.decision(), result.riskScore(), refund, result.reasonCode(), result.createdAt());
    }
}
