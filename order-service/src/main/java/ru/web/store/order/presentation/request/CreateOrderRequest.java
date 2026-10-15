package ru.web.store.order.presentation.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull(message = "customerId обязателен")
        UUID customerId,

        @NotBlank(message = "currency обязательна")
        String currency,

        @NotEmpty(message = "Список товаров не может быть пустым")
        @Valid
        List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            @NotNull UUID productId,
            @Positive(message = "Количество должно быть > 0") int quantity,
            @NotNull @Positive(message = "Цена должна быть > 0") BigDecimal price
    ) {}
}