package ru.web.store.inventory;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import ru.web.store.inventory.domain.Product;

class ProductTest {
  @Test
  void negativeStockIsRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Product(UUID.randomUUID(), "Book", BigDecimal.TEN, "RUB", -1));
  }

  @Test
  void negativePriceIsRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Product(UUID.randomUUID(), "Book", BigDecimal.TEN.negate(), "RUB", 1));
  }
}
