package parallax.vn.springbootdemo.application.customer.usecase;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import parallax.vn.springbootdemo.application.customer.dto.CustomerHistory;
import parallax.vn.springbootdemo.application.customer.port.in.GetCustomerOrderHistoryUseCase;
import parallax.vn.springbootdemo.application.customer.port.out.CustomerRepositoryPort;
import parallax.vn.springbootdemo.common.annotation.UseCase;
import parallax.vn.springbootdemo.domain.customer.aggregate.Customer;
import parallax.vn.springbootdemo.domain.customer.exception.CustomerNotFoundException;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;

/** Serves the inbound parallax hop; result cached in Redis ("customerHistory", TTL 5m). */
@UseCase
public class GetCustomerOrderHistoryService implements GetCustomerOrderHistoryUseCase {

    private final CustomerRepositoryPort customers;

    public GetCustomerOrderHistoryService(CustomerRepositoryPort customers) {
        this.customers = customers;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "customerHistory", key = "#id.value().toString()")
    public CustomerHistory getHistory(CustomerId id) {
        Customer c = customers.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id.value().toString()));
        return new CustomerHistory(
                c.id().value(),
                c.status().name(),
                c.tier().name(),
                c.lifetimeOrderCount(),
                c.lifetimeGmv().amount(),
                c.lifetimeGmv().currency(),
                c.lastOrderAt());
    }
}
