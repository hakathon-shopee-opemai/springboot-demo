package parallax.vn.springbootdemo.application.customer.port.out;

import java.util.Optional;
import parallax.vn.springbootdemo.domain.customer.aggregate.Customer;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;
import parallax.vn.springbootdemo.domain.customer.vo.Email;

public interface CustomerRepositoryPort {
    Customer save(Customer customer);

    Optional<Customer> findById(CustomerId id);

    boolean existsByEmail(Email email);
}
