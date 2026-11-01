package parallax.vn.springbootdemo.adapter.out.persistence.order.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import parallax.vn.springbootdemo.adapter.out.persistence.order.entity.RefundLedgerEntity;

public interface RefundLedgerJpaRepository extends JpaRepository<RefundLedgerEntity, UUID> {
    Optional<RefundLedgerEntity> findByReturnId(UUID returnId);
}
