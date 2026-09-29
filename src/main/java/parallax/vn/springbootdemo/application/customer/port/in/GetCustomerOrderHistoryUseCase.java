package parallax.vn.springbootdemo.application.customer.port.in;

import parallax.vn.springbootdemo.application.customer.dto.CustomerHistory;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;

public interface GetCustomerOrderHistoryUseCase {
    CustomerHistory getHistory(CustomerId id);
}
