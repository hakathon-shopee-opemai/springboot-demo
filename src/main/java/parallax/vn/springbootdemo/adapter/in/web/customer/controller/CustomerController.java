package parallax.vn.springbootdemo.adapter.in.web.customer.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import parallax.vn.springbootdemo.adapter.in.web.customer.dto.request.CreateCustomerRequest;
import parallax.vn.springbootdemo.adapter.in.web.customer.dto.response.CustomerResponse;
import parallax.vn.springbootdemo.adapter.in.web.customer.mapper.CustomerWebMapper;
import parallax.vn.springbootdemo.application.customer.dto.CreateCustomerCommand;
import parallax.vn.springbootdemo.application.customer.port.in.CreateCustomerUseCase;
import parallax.vn.springbootdemo.application.customer.port.in.GetCustomerUseCase;
import parallax.vn.springbootdemo.common.annotation.WebAdapter;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;

@WebAdapter
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CreateCustomerUseCase createCustomer;
    private final GetCustomerUseCase getCustomer;
    private final CustomerWebMapper mapper;

    public CustomerController(CreateCustomerUseCase createCustomer, GetCustomerUseCase getCustomer,
            CustomerWebMapper mapper) {
        this.createCustomer = createCustomer;
        this.getCustomer = getCustomer;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        var customer = createCustomer.create(new CreateCustomerCommand(request.email(),
                request.fullName(), request.phone(), request.tier(), request.accountStatus()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(customer));
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable UUID id) {
        return mapper.toResponse(getCustomer.get(CustomerId.of(id)));
    }
}
