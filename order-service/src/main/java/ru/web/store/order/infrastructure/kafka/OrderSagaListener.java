package ru.web.store.order.infrastructure.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.web.store.contracts.*;
import ru.web.store.order.application.usecase.ApplySagaResult;

@Component
public class OrderSagaListener {
  private final ApplySagaResult result;

  public OrderSagaListener(ApplySagaResult result) {
    this.result = result;
  }

  @KafkaListener(topics = "store.inventory.v1")
  public void receive(String payload) {
    result.execute(Json.read(payload, StoreEvent.class));
  }
}
