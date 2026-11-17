package ru.web.store.order.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record Coupon(UUID id, String code, int percent) {
  public Coupon {
    if (id == null || code == null || code.isBlank() || percent < 1 || percent > 50)
      throw new IllegalArgumentException("Discount must be 1..50 percent");
  }

  public BigDecimal apply(BigDecimal amount) {
    return amount
        .multiply(BigDecimal.valueOf(100 - percent))
        .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
  }
}
