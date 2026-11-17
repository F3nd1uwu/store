package ru.web.store.order.application.usecase;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.web.store.contracts.StoreEvent;
import ru.web.store.order.application.dto.CreateOrderCommand;
import ru.web.store.order.application.port.*;
import ru.web.store.order.domain.model.*;
import ru.web.store.order.domain.repository.OrderRepository;

@Service
public class CreateOrderUseCase {
  private final OrderRepository orders;
  private final OrderEvents events;
  private final CatalogPort catalog;

  public CreateOrderUseCase(OrderRepository orders, OrderEvents events, CatalogPort catalog) {
    this.orders = orders;
    this.events = events;
    this.catalog = catalog;
  }

  @Transactional
  public Order execute(CreateOrderCommand command) {
    var items =
        command.items().stream()
            .map(
                i -> {
                  var quote = catalog.quote(i.productId());
                  if (!quote.currency().equals(command.currency()))
                    throw new IllegalArgumentException("Currency mismatch");
                  return new OrderItem(
                      UUID.randomUUID(),
                      i.productId(),
                      i.quantity(),
                      Money.of(quote.price(), quote.currency()));
                })
            .toList();
    Order order =
        orders.save(new Order(UUID.randomUUID(), command.customerId(), items, command.currency()));
    events.append(
        new StoreEvent(
            UUID.randomUUID(),
            "ORDER_CREATED",
            order.getId(),
            order.getCustomerId(),
            order.getTotalAmount().amount(),
            command.currency(),
            items.stream()
                .map(i -> new StoreEvent.Line(i.getProductId(), i.getQuantity()))
                .toList(),
            null));
    return order;
  }
}
