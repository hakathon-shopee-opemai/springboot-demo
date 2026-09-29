package parallax.vn.springbootdemo.adapter.out.persistence.order.adapter;

import java.util.Optional;
import parallax.vn.springbootdemo.adapter.out.persistence.order.mapper.OrderPersistenceMapper;
import parallax.vn.springbootdemo.adapter.out.persistence.order.repository.OrderJpaRepository;
import parallax.vn.springbootdemo.application.order.port.out.OrderRepositoryPort;
import parallax.vn.springbootdemo.common.annotation.PersistenceAdapter;
import parallax.vn.springbootdemo.domain.order.aggregate.Order;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;

@PersistenceAdapter
public class OrderPersistenceAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository repository;
    private final OrderPersistenceMapper mapper;

    public OrderPersistenceAdapter(OrderJpaRepository repository, OrderPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Order save(Order order) {
        return mapper.toDomain(repository.save(mapper.toEntity(order)));
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return repository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public long countByCustomerId(parallax.vn.springbootdemo.domain.customer.vo.CustomerId customerId) {
        return repository.countByCustomerId(customerId.value());
    }
}
