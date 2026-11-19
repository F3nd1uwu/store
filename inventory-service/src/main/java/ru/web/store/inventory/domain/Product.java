package ru.web.store.inventory.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record Product(UUID id, String title, BigDecimal price, String currency, int quantity)
    implements java.io.Serializable {
  public Product {
    if (id == null
        || title == null
        || title.isBlank()
        || price == null
        || price.signum() < 0
        || currency == null
        || !currency.matches("[A-Z]{3}")
        || quantity < 0) throw new IllegalArgumentException("Некорректный товар");
  }
}
