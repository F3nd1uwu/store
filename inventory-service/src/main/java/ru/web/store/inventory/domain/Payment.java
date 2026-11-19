package ru.web.store.inventory.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record Payment(UUID orderId, UUID customerId, BigDecimal amount, String currency) {
  public Payment {
    if (orderId == null
        || customerId == null
        || amount == null
        || amount.signum() < 0
        || currency == null) throw new IllegalArgumentException("Invalid payment");
  }
}
