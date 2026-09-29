package parallax.vn.springbootdemo.adapter.out.persistence.order.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Component;
import parallax.vn.springbootdemo.adapter.out.persistence.order.entity.OrderCancellationEntity;
import parallax.vn.springbootdemo.domain.order.aggregate.OrderCancellation;
import parallax.vn.springbootdemo.domain.order.vo.CancellationStatus;
import parallax.vn.springbootdemo.domain.order.vo.OrderId;
import parallax.vn.springbootdemo.domain.order.vo.SagaStep;

@Component
public class OrderCancellationPersistenceMapper {

    private static final TypeReference<List<SagaStep>> STEP_LIST = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public OrderCancellationPersistenceMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OrderCancellationEntity toEntity(OrderCancellation c) {
        OrderCancellationEntity e = new OrderCancellationEntity();
        e.setId(c.id());
        e.setOrderId(c.orderId().value());
        e.setStatus(c.status().name());
        e.setReason(c.reason());
        e.setRequestedBy(c.requestedBy());
        e.setSagaSteps(writeSteps(c.steps()));
        e.setIdempotencyKey(c.idempotencyKey());
        e.setCreatedAt(c.createdAt());
        e.setUpdatedAt(c.updatedAt());
        e.setVersion(c.version());
        return e;
    }

    public OrderCancellation toDomain(OrderCancellationEntity e) {
        return OrderCancellation.rehydrate(
                e.getId(),
                OrderId.of(e.getOrderId()),
                CancellationStatus.valueOf(e.getStatus()),
                e.getReason(),
                e.getRequestedBy(),
                readSteps(e.getSagaSteps()),
                e.getIdempotencyKey(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getVersion());
    }

    private String writeSteps(List<SagaStep> steps) {
        try {
            return objectMapper.writeValueAsString(steps);
        } catch (Exception ex) {
            return "[]";
        }
    }

    private List<SagaStep> readSteps(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, STEP_LIST);
        } catch (Exception ex) {
            return List.of();
        }
    }
}
