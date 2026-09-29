package parallax.vn.springbootdemo.application.order.port.in;

import parallax.vn.springbootdemo.application.order.dto.CustomerReturnHistory;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;

public interface GetCustomerReturnHistoryUseCase {
    CustomerReturnHistory getReturnHistory(CustomerId customerId);
}
