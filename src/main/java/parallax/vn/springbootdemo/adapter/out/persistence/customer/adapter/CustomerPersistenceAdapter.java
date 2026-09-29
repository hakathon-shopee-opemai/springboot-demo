package parallax.vn.springbootdemo.adapter.out.persistence.customer.adapter;

import java.util.Optional;
import parallax.vn.springbootdemo.adapter.out.persistence.customer.entity.CustomerEntity;
import parallax.vn.springbootdemo.adapter.out.persistence.customer.mapper.CustomerPersistenceMapper;
import parallax.vn.springbootdemo.adapter.out.persistence.customer.repository.CustomerJpaRepository;
import parallax.vn.springbootdemo.application.customer.port.out.CustomerRepositoryPort;
import parallax.vn.springbootdemo.common.annotation.PersistenceAdapter;
import parallax.vn.springbootdemo.domain.customer.aggregate.Customer;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;
import parallax.vn.springbootdemo.domain.customer.vo.Email;

@PersistenceAdapter
public class CustomerPersistenceAdapter implements CustomerRepositoryPort {

    private final CustomerJpaRepository repository;
    private final CustomerPersistenceMapper mapper;

    public CustomerPersistenceAdapter(CustomerJpaRepository repository, CustomerPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Customer save(Customer customer) {
        CustomerEntity saved = repository.save(mapper.toEntity(customer));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Customer> findById(CustomerId id) {
        return repository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return repository.existsByEmail(email.value());
    }
}
