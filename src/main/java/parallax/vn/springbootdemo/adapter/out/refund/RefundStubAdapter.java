package parallax.vn.springbootdemo.adapter.out.refund;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parallax.vn.springbootdemo.application.order.port.out.RefundPort;
import parallax.vn.springbootdemo.common.annotation.PersistenceAdapter;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;

/** Stubbed refund (always succeeds) — real payment integration is out of scope. */
@PersistenceAdapter
public class RefundStubAdapter implements RefundPort {

    private static final Logger log = LoggerFactory.getLogger(RefundStubAdapter.class);

    @Override
    public void refund(OrderId orderId) {
        log.debug("refund (stub) order={}", orderId.value());
    }

    @Override
    public void reverseRefund(OrderId orderId) {
        log.debug("reverse refund (stub) order={}", orderId.value());
    }
}
