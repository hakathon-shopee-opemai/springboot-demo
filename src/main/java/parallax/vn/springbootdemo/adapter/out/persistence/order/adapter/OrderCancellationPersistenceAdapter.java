package parallax.vn.springbootdemo.adapter.out.persistence.order.adapter;

import java.util.Optional;
import parallax.vn.springbootdemo.adapter.out.persistence.order.mapper.OrderCancellationPersistenceMapper;
import parallax.vn.springbootdemo.adapter.out.persistence.order.repository.OrderCancellationJpaRepository;
import parallax.vn.springbootdemo.application.order.port.out.OrderCancellationRepositoryPort;
import parallax.vn.springbootdemo.common.annotation.PersistenceAdapter;
import parallax.vn.springbootdemo.domain.order.aggregate.OrderCancellation;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;

@PersistenceAdapter
public class OrderCancellationPersistenceAdapter implements OrderCancellationRepositoryPort {

    private final OrderCancellationJpaRepository repository;
    private final OrderCancellationPersistenceMapper mapper;

    public OrderCancellationPersistenceAdapter(OrderCancellationJpaRepository repository,
            OrderCancellationPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<OrderCancellation> findByOrderId(OrderId orderId) {
        return repository.findByOrderId(orderId.value()).map(mapper::toDomain);
    }

    @Override
    public OrderCancellation save(OrderCancellation cancellation) {
        return mapper.toDomain(repository.save(mapper.toEntity(cancellation)));
    }
}
