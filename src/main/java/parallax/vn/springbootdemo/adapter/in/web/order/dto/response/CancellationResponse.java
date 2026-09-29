package parallax.vn.springbootdemo.adapter.in.web.order.dto.response;

import java.util.List;
import java.util.UUID;

public record CancellationResponse(UUID orderId, String status, String sagaStatus, String reason,
        List<Step> steps) {

    public record Step(String name, String status) {
    }
}
