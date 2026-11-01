package parallax.vn.springbootdemo.adapter.out.persistence.order.adapter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import parallax.vn.springbootdemo.adapter.out.persistence.order.entity.OrderReturnEntity;
import parallax.vn.springbootdemo.adapter.out.persistence.order.entity.RefundLedgerEntity;
import parallax.vn.springbootdemo.adapter.out.persistence.order.mapper.OrderReturnPersistenceMapper;
import parallax.vn.springbootdemo.adapter.out.persistence.order.repository.OrderReturnJpaRepository;
import parallax.vn.springbootdemo.adapter.out.persistence.order.repository.RefundLedgerJpaRepository;
import parallax.vn.springbootdemo.application.order.dto.ReturnHistoryStats;
import parallax.vn.springbootdemo.application.order.port.out.ReturnRepositoryPort;
import parallax.vn.springbootdemo.common.annotation.PersistenceAdapter;
import parallax.vn.springbootdemo.domain.customer.vo.CustomerId;
import parallax.vn.springbootdemo.domain.order.aggregate.OrderReturn;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;
import parallax.vn.springbootdemo.domain.order.vo.ReturnStatus;

@PersistenceAdapter
public class OrderReturnPersistenceAdapter implements ReturnRepositoryPort {

    private static final String DEFAULT_CURRENCY = "USD";
    private static final List<String> ACTIVE_STATUSES =
            List.of(ReturnStatus.REFUNDED.name(), ReturnStatus.UNDER_REVIEW.name());

    private final OrderReturnJpaRepository returnRepository;
    private final RefundLedgerJpaRepository refundRepository;
    private final OrderReturnPersistenceMapper mapper;

    public OrderReturnPersistenceAdapter(OrderReturnJpaRepository returnRepository,
            RefundLedgerJpaRepository refundRepository, OrderReturnPersistenceMapper mapper) {
        this.returnRepository = returnRepository;
        this.refundRepository = refundRepository;
        this.mapper = mapper;
    }

    @Override
    public OrderReturn save(OrderReturn orderReturn) {
        OrderReturnEntity saved = returnRepository.save(mapper.toEntity(orderReturn));
        RefundLedgerEntity refund = null;
        if (orderReturn.refund() != null) {
            // one refund row per return (idempotent)
            refund = refundRepository.findByReturnId(orderReturn.id())
                    .orElseGet(() -> refundRepository.save(mapper.toRefundEntity(orderReturn.refund())));
        }
        return mapper.toDomain(saved, refund);
    }

    @Override
    public Optional<OrderReturn> findById(UUID id) {
        return returnRepository.findById(id)
                .map(e -> mapper.toDomain(e, refundRepository.findByReturnId(id).orElse(null)));
    }

    @Override
    public boolean existsActiveReturn(OrderId orderId) {
        return returnRepository.existsByOrderIdAndStatusIn(orderId.value(), ACTIVE_STATUSES);
    }

    @Override
    public List<OrderReturn> findUnderReviewBefore(Instant cutoff) {
        return returnRepository.findTop50ByStatusAndCreatedAtBeforeOrderByCreatedAtAsc(
                        ReturnStatus.UNDER_REVIEW.name(), cutoff).stream()
                .map(e -> mapper.toDomain(e, null))
                .toList();
    }

    @Override
    public ReturnHistoryStats statsByCustomer(CustomerId customerId, Instant since) {
        UUID cid = customerId.value();
        long totalReturns = returnRepository.countByCustomerId(cid);
        BigDecimal refunded = returnRepository.sumRefundedAmount(cid);
        long returnsSince = returnRepository.countByCustomerIdAndCreatedAtGreaterThanEqual(cid, since);
        Instant lastReturnAt = returnRepository.findLastReturnAt(cid);
        return new ReturnHistoryStats(totalReturns,
                refunded == null ? BigDecimal.ZERO : refunded,
                DEFAULT_CURRENCY, returnsSince, lastReturnAt);
    }
}
