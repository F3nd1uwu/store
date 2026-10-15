package ru.web.store.order.presentation.response;

import ru.web.store.order.domain.model.Order;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        UUID customerId,
        String status,
        BigDecimal totalAmount,
        String currency,
        Instant createdAt
) {
    public static OrderResponse fromDomain(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus().name(),
                order.getTotalAmount().amount(),
                order.getTotalAmount().currency(),
                order.getCreatedAt()
        );
    }
}