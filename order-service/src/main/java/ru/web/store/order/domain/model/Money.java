package ru.web.store.order.domain.model;

import java.math.BigDecimal;

public record Money(BigDecimal amount, String currency) implements java.io.Serializable {
    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Сумма не может быть отрицательной или null");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Валюта обязательна");
        }
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Нельзя складывать разные валюты");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
}