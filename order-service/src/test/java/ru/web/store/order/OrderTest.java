package ru.web.store.order;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import ru.web.store.order.domain.model.*;

class OrderTest {
  @Test
  void restorationPreservesCancelledStatusAndCreationTime() {
    Instant date = Instant.parse("2020-01-01T00:00:00Z");
    Order order =
        Order.restore(
            UUID.randomUUID(),
            UUID.randomUUID(),
            List.of(
                new OrderItem(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    2,
                    Money.of(new BigDecimal("5.00"), "RUB"))),
            "RUB",
            OrderStatus.CANCELLED,
            date);
    assertEquals(OrderStatus.CANCELLED, order.getStatus());
    assertEquals(date, order.getCreatedAt());
    assertEquals(0, new BigDecimal("10.00").compareTo(order.getTotalAmount().amount()));
  }

  @Test
  void completedOrderCannotBeCancelled() {
    Order order =
        Order.restore(
            UUID.randomUUID(),
            UUID.randomUUID(),
            List.of(
                new OrderItem(
                    UUID.randomUUID(), UUID.randomUUID(), 1, Money.of(BigDecimal.ONE, "RUB"))),
            "RUB",
            OrderStatus.COMPLETED,
            Instant.now());
    assertThrows(IllegalStateException.class, order::cancel);
  }
}
