package parallax.vn.springbootdemo.application.customer.usecase;

import org.springframework.transaction.annotation.Transactional;
import parallax.vn.springbootdemo.application.customer.port.in.GetCustomerUseCase;
import parallax.vn.springbootdemo.application.customer.port.out.CustomerRepositoryPort;
import parallax.vn.springbootdemo.common.annotation.UseCase;
import parallax.vn.springbootdemo.domain.customer.aggregate.Customer;
import parallax.vn.springbootdemo.domain.customer.exception.CustomerNotFoundException;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;

@UseCase
public class GetCustomerService implements GetCustomerUseCase {

    private final CustomerRepositoryPort customers;

    public GetCustomerService(CustomerRepositoryPort customers) {
        this.customers = customers;
    }

    @Override
    @Transactional(readOnly = true)
    public Customer get(CustomerId id) {
        return customers.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id.value().toString()));
    }
}
