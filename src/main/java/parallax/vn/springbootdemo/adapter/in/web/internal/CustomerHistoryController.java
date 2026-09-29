package parallax.vn.springbootdemo.adapter.in.web.internal;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import parallax.vn.springbootdemo.application.customer.dto.CustomerHistory;
import parallax.vn.springbootdemo.application.customer.port.in.GetCustomerOrderHistoryUseCase;
import parallax.vn.springbootdemo.common.annotation.WebAdapter;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;

/** Inbound parallax hop: risk-service fetches authoritative customer history (HMAC-protected). */
@WebAdapter
@RestController
public class CustomerHistoryController {

    private final GetCustomerOrderHistoryUseCase getHistory;

    public CustomerHistoryController(GetCustomerOrderHistoryUseCase getHistory) {
        this.getHistory = getHistory;
    }

    @GetMapping("/internal/customers/{customerId}/order-history")
    public CustomerHistory orderHistory(@PathVariable UUID customerId) {
        return getHistory.getHistory(CustomerId.of(customerId));
    }
}
