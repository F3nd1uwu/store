package ru.web.store.inventory.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record Wallet(UUID customerId, String currency, BigDecimal balance) {
  public Wallet {
    if (customerId == null
        || currency == null
        || !currency.matches("[A-Z]{3}")
        || balance == null
        || balance.signum() < 0) throw new IllegalArgumentException("Invalid wallet");
  }
}
