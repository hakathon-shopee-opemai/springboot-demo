package parallax.vn.springbootdemo.application.customer.usecase;

import parallax.vn.springbootdemo.application.customer.dto.CreateCustomerCommand;
import parallax.vn.springbootdemo.application.customer.port.in.CreateCustomerUseCase;
import parallax.vn.springbootdemo.application.customer.port.out.CustomerRepositoryPort;
import parallax.vn.springbootdemo.common.annotation.UseCase;
import parallax.vn.springbootdemo.common.exception.code.ErrorCode;
import parallax.vn.springbootdemo.common.exception.model.ApplicationException;
import parallax.vn.springbootdemo.domain.customer.aggregate.Customer;
import parallax.vn.springbootdemo.domain.customer.vo.AccountStatus;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerTier;
import parallax.vn.springbootdemo.domain.customer.vo.Email;
import org.springframework.transaction.annotation.Transactional;

@UseCase
public class CreateCustomerService implements CreateCustomerUseCase {

    private final CustomerRepositoryPort customers;

    public CreateCustomerService(CustomerRepositoryPort customers) {
        this.customers = customers;
    }

    @Override
    @Transactional
    public Customer create(CreateCustomerCommand command) {
        Email email = Email.of(command.email());
        if (customers.existsByEmail(email)) {
            throw new ApplicationException(ErrorCode.CONFLICT, "email already registered: " + command.email());
        }
        CustomerTier tier = command.tier() == null ? CustomerTier.STANDARD
                : CustomerTier.valueOf(command.tier());
        AccountStatus status = command.accountStatus() == null ? AccountStatus.ACTIVE
                : AccountStatus.valueOf(command.accountStatus());
        Customer customer = Customer.register(email, command.fullName(), command.phone(), tier, status);
        return customers.save(customer);
    }
}
