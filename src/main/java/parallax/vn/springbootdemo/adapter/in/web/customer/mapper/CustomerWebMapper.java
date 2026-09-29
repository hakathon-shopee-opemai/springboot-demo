package parallax.vn.springbootdemo.adapter.in.web.customer.mapper;

import org.springframework.stereotype.Component;
import parallax.vn.springbootdemo.adapter.in.web.customer.dto.response.CustomerResponse;
import parallax.vn.springbootdemo.domain.customer.aggregate.Customer;

@Component
public class CustomerWebMapper {

    public CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(
                c.id().value(),
                c.email().value(),
                c.fullName(),
                c.phone(),
                c.tier().name(),
                c.status().name(),
                c.lifetimeOrderCount(),
                c.lifetimeGmv().amount(),
                c.lifetimeGmv().currency(),
                c.createdAt());
    }
}
