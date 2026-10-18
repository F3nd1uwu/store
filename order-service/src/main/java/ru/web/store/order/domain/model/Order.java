package ru.web.store.order.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Order implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private final UUID customerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private Money totalAmount;
    private final Instant createdAt;

    public Order(UUID id, UUID customerId, List<OrderItem> items, String currency) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Заказ не может быть пустым");
        }
        this.id = id != null ? id : UUID.randomUUID();
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.status = OrderStatus.NEW;
        this.createdAt = Instant.now();
        this.totalAmount = calculateTotal(currency);
    }

    private Money calculateTotal(String currency) {
        BigDecimal sum = items.stream()
                .map(item -> item.calculateSubtotal().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Money.of(sum, currency);
    }

    public void cancel() {
        if (this.status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Нельзя отменить завершенный заказ");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public OrderStatus getStatus() { return status; }
    public Money getTotalAmount() { return totalAmount; }
    public Instant getCreatedAt() { return createdAt; }
}