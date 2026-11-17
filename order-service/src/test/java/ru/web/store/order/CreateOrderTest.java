package ru.web.store.order;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import ru.web.store.order.application.dto.CreateOrderCommand;
import ru.web.store.order.application.port.*;
import ru.web.store.order.application.usecase.CreateOrderUseCase;
import ru.web.store.order.domain.repository.OrderRepository;

class CreateOrderTest {
  @Test
  void priceComesFromCatalogAndOutboxContainsItems() {
    var repository = mock(OrderRepository.class);
    when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
    var events = mock(OrderEvents.class);
    var catalog = mock(CatalogPort.class);
    var product = UUID.randomUUID();
    when(catalog.quote(product)).thenReturn(new CatalogPort.Quote(BigDecimal.TEN, "RUB", 5));
    var command =
        new CreateOrderCommand(
            UUID.randomUUID(),
            "RUB",
            List.of(new CreateOrderCommand.ItemCommand(product, 2, BigDecimal.ONE)));
    var order = new CreateOrderUseCase(repository, events, catalog).execute(command);
    assertEquals(0, new BigDecimal("20").compareTo(order.getTotalAmount().amount()));
    verify(events)
        .append(
            argThat(e -> e.items().getFirst().quantity() == 2 && e.type().equals("ORDER_CREATED")));
  }

  @Test
  void currencyMismatchDoesNotPersistOrder() {
    var repository = mock(OrderRepository.class);
    var catalog = mock(CatalogPort.class);
    when(catalog.quote(any())).thenReturn(new CatalogPort.Quote(BigDecimal.ONE, "USD", 1));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new CreateOrderUseCase(repository, mock(OrderEvents.class), catalog)
                .execute(
                    new CreateOrderCommand(
                        UUID.randomUUID(),
                        "RUB",
                        List.of(
                            new CreateOrderCommand.ItemCommand(
                                UUID.randomUUID(), 1, BigDecimal.ONE)))));
    verify(repository, never()).save(any());
  }
}
