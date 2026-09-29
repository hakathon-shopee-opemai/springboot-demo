package parallax.vn.springbootdemo.adapter.out.client.risk.dto.response;

import java.util.UUID;

public record VelocityAdjustResponse(UUID customerId, long previousCount, long newCount,
        boolean applied) {
}
