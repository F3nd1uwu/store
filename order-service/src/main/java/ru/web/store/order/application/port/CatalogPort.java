package ru.web.store.order.application.port;

import java.math.BigDecimal;
import java.util.UUID;

public interface CatalogPort {
  record Quote(BigDecimal price, String currency, int quantity) {}

  Quote quote(UUID productId);
}
