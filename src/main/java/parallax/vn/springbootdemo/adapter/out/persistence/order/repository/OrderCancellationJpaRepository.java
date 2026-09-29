package parallax.vn.springbootdemo.adapter.out.persistence.order.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import parallax.vn.springbootdemo.adapter.out.persistence.order.entity.OrderCancellationEntity;

public interface OrderCancellationJpaRepository extends JpaRepository<OrderCancellationEntity, UUID> {
    Optional<OrderCancellationEntity> findByOrderId(UUID orderId);
}
