package ru.web.store.contracts;

import java.math.BigDecimal;
import java.util.*;

public record StoreEvent(
    UUID eventId,
    String type,
    UUID orderId,
    UUID customerId,
    BigDecimal amount,
    String currency,
    List<Line> items,
    String reason) {
  public record Line(UUID productId, int quantity) {}

  public StoreEvent next(String type, String reason) {
    return new StoreEvent(
        UUID.randomUUID(), type, orderId, customerId, amount, currency, items, reason);
  }
}
