package parallax.vn.springbootdemo.adapter.in.web.internal;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import parallax.vn.springbootdemo.application.order.dto.CustomerReturnHistory;
import parallax.vn.springbootdemo.application.order.port.in.GetCustomerReturnHistoryUseCase;
import parallax.vn.springbootdemo.common.annotation.WebAdapter;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;

/** Inbound parallax hop: risk-service fetches authoritative return history (HMAC-protected). */
@WebAdapter
@RestController
public class CustomerReturnHistoryController {

    private final GetCustomerReturnHistoryUseCase getReturnHistory;

    public CustomerReturnHistoryController(GetCustomerReturnHistoryUseCase getReturnHistory) {
        this.getReturnHistory = getReturnHistory;
    }

    @GetMapping("/internal/customers/{customerId}/return-history")
    public CustomerReturnHistory returnHistory(@PathVariable UUID customerId) {
        return getReturnHistory.getReturnHistory(CustomerId.of(customerId));
    }
}
