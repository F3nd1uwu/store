package ru.web.store.order.infrastructure.persistence;

import java.time.Instant;
import org.springframework.stereotype.Component;
import ru.web.store.contracts.*;
import ru.web.store.order.application.port.OrderEvents;
import ru.web.store.order.infrastructure.persistence.entity.OutboxEventJpaEntity;

@Component
public class OrderEventsAdapter implements OrderEvents {
  private final SpringDataOutboxRepository events;

  public OrderEventsAdapter(SpringDataOutboxRepository events) {
    this.events = events;
  }

  public void append(StoreEvent event) {
    events.save(
        new OutboxEventJpaEntity(
            event.eventId(),
            "ORDER",
            event.orderId(),
            event.type(),
            Json.write(event),
            "PENDING",
            Instant.now()));
  }
}
