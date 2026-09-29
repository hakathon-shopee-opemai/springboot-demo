package parallax.vn.springbootdemo.application.customer.port.in;

import parallax.vn.springbootdemo.domain.customer.aggregate.Customer;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;

public interface GetCustomerUseCase {
    Customer get(CustomerId id);
}
