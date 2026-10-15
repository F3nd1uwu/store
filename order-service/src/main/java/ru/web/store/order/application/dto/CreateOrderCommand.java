package ru.web.store.order.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(
        UUID customerId,
        String currency,
        List<ItemCommand> items
) {
    public record ItemCommand(UUID productId, int quantity, BigDecimal unitPrice) {}
}