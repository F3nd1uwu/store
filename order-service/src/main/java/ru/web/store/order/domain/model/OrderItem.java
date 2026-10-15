package ru.web.store.order.domain.model;

import java.util.Objects;
import java.util.UUID;

public class OrderItem {
    private final UUID id;
    private final UUID productId;
    private final int quantity;
    private final Money unitPrice;

    public OrderItem(UUID id, UUID productId, int quantity, Money unitPrice) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество товара должно быть больше 0");
        }
        this.id = id != null ? id : UUID.randomUUID();
        this.productId = Objects.requireNonNull(productId, "ProductId обязателен");
        this.quantity = quantity;
        this.unitPrice = Objects.requireNonNull(unitPrice, "Цена обязательна");
    }

    public Money calculateSubtotal() {
        return new Money(unitPrice.amount().multiply(java.math.BigDecimal.valueOf(quantity)), unitPrice.currency());
    }

    public UUID getId() { return id; }
    public UUID getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public Money getUnitPrice() { return unitPrice; }
}