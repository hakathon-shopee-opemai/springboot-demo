package parallax.vn.springbootdemo.adapter.out.persistence.customer.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerJpaRepository extends JpaRepository<
        parallax.vn.springbootdemo.adapter.out.persistence.customer.entity.CustomerEntity, UUID> {
    boolean existsByEmail(String email);
}
