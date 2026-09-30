package parallax.vn.springbootdemo.adapter.out.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.resilience.annotation.Retryable;
import parallax.vn.springbootdemo.application.order.port.out.InventoryPort;
import parallax.vn.springbootdemo.common.annotation.PersistenceAdapter;
import parallax.vn.springbootdemo.domain.order.entity.OrderLine;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;

/** Stubbed inventory (always succeeds) — real reservation is out of scope for the demo. */
@PersistenceAdapter
public class InventoryStubAdapter implements InventoryPort {

    private static final Logger log = LoggerFactory.getLogger(InventoryStubAdapter.class);

    @Override
    @Retryable(includes = TransientDataAccessException.class, maxRetries = 2, delay = 50)
    public void reserveLine(OrderId orderId, OrderLine line) {
        log.debug("reserve inventory (stub) order={} product={} qty={}", orderId.value(),
                line.productId(), line.quantity());
    }

    @Override
    public void release(OrderId orderId) {
        log.debug("release inventory (stub) order={}", orderId.value());
    }
}
