package ru.web.store.order.domain.model;

public enum OrderStatus {
    NEW,
    PENDING_PAYMENT,
    PAID,
    CANCELLED,
    COMPLETED
}