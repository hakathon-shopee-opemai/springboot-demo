package parallax.vn.springbootdemo.application.order.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import parallax.vn.springbootdemo.application.customer.port.out.CustomerRepositoryPort;
import parallax.vn.springbootdemo.application.order.port.out.OrderCancellationRepositoryPort;
import parallax.vn.springbootdemo.application.order.port.out.OrderRepositoryPort;
import parallax.vn.springbootdemo.domain.customer.aggregate.Customer;
import parallax.vn.springbootdemo.domain.order.aggregate.Order;
import parallax.vn.springbootdemo.domain.order.aggregate.OrderCancellation;

/**
 * Commits the terminal state of a successful cancellation in a single DB transaction
 * (order + customer stats + saga log). Kept separate from the saga so no DB transaction
 * is held open across the remote (risk-service) calls.
 */
@Component
public class CancellationFinalizer {

    private final OrderRepositoryPort orders;
    private final CustomerRepositoryPort customers;
    private final OrderCancellationRepositoryPort cancellations;

    public CancellationFinalizer(OrderRepositoryPort orders, CustomerRepositoryPort customers,
            OrderCancellationRepositoryPort cancellations) {
        this.orders = orders;
        this.customers = customers;
        this.cancellations = cancellations;
    }

    @Transactional
    public OrderCancellation finalizeSuccess(Order order, Customer customer,
            OrderCancellation cancellation) {
        orders.save(order);
        customers.save(customer);
        cancellation.complete();
        return cancellations.save(cancellation);
    }
}
