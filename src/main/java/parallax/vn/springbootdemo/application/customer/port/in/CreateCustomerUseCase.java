package parallax.vn.springbootdemo.application.customer.port.in;

import parallax.vn.springbootdemo.application.customer.dto.CreateCustomerCommand;
import parallax.vn.springbootdemo.domain.customer.aggregate.Customer;

public interface CreateCustomerUseCase {
    Customer create(CreateCustomerCommand command);
}
