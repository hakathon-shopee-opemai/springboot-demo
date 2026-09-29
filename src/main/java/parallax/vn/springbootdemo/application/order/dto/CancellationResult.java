package parallax.vn.springbootdemo.application.order.dto;

import java.util.List;
import java.util.UUID;

/** Outcome of a cancellation saga. */
public record CancellationResult(UUID orderId, String status, String sagaStatus, String reason,
        List<Step> steps) {

    public record Step(String name, String status) {
    }
}
