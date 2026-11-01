package parallax.vn.springbootdemo.adapter.out.persistence.order.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import parallax.vn.springbootdemo.adapter.out.persistence.order.entity.OrderReturnEntity;

public interface OrderReturnJpaRepository extends JpaRepository<OrderReturnEntity, UUID> {

    boolean existsByOrderIdAndStatusIn(UUID orderId, Collection<String> statuses);

    long countByCustomerId(UUID customerId);

    List<OrderReturnEntity> findTop50ByStatusAndCreatedAtBeforeOrderByCreatedAtAsc(String status,
            Instant cutoff);

    long countByCustomerIdAndCreatedAtGreaterThanEqual(UUID customerId, Instant since);

    @Query("select coalesce(sum(r.requestedAmount), 0) from OrderReturnEntity r "
            + "where r.customerId = :customerId and r.status = 'REFUNDED'")
    BigDecimal sumRefundedAmount(@Param("customerId") UUID customerId);

    @Query("select max(r.createdAt) from OrderReturnEntity r where r.customerId = :customerId")
    Instant findLastReturnAt(@Param("customerId") UUID customerId);
}
