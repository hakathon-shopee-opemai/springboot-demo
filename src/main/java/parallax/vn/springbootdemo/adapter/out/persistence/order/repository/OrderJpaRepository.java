package parallax.vn.springbootdemo.adapter.out.persistence.order.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import parallax.vn.springbootdemo.adapter.out.persistence.order.entity.OrderEntity;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {
}
