package parallax.vn.springbootdemo.adapter.out.persistence.customer.mapper;

import org.springframework.stereotype.Component;
import parallax.vn.springbootdemo.adapter.out.persistence.customer.entity.CustomerEntity;
import parallax.vn.springbootdemo.domain.customer.aggregate.Customer;
import parallax.vn.springbootdemo.domain.customer.vo.AccountStatus;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerTier;
import parallax.vn.springbootdemo.domain.customer.vo.Email;
import parallax.vn.springbootdemo.domain.shared.vo.Money;

@Component
public class CustomerPersistenceMapper {

    public CustomerEntity toEntity(Customer c) {
        CustomerEntity e = new CustomerEntity();
        e.setId(c.id().value());
        e.setEmail(c.email().value());
        e.setFullName(c.fullName());
        e.setPhone(c.phone());
        e.setAccountStatus(c.status().name());
        e.setTier(c.tier().name());
        e.setLifetimeOrderCount(c.lifetimeOrderCount());
        e.setLifetimeGmvAmount(c.lifetimeGmv().amount());
        e.setLifetimeGmvCurrency(c.lifetimeGmv().currency());
        e.setLastOrderAt(c.lastOrderAt());
        e.setCreatedAt(c.createdAt());
        e.setUpdatedAt(c.updatedAt());
        e.setVersion(c.version());
        return e;
    }

    public Customer toDomain(CustomerEntity e) {
        return Customer.rehydrate(
                CustomerId.of(e.getId()),
                Email.of(e.getEmail()),
                e.getFullName(),
                e.getPhone(),
                AccountStatus.valueOf(e.getAccountStatus()),
                CustomerTier.valueOf(e.getTier()),
                e.getLifetimeOrderCount(),
                Money.of(e.getLifetimeGmvAmount(), e.getLifetimeGmvCurrency()),
                e.getLastOrderAt(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getVersion());
    }
}
