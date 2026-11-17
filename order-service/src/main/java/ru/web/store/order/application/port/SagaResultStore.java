package ru.web.store.order.application.port;

import ru.web.store.contracts.StoreEvent;

public interface SagaResultStore {
  void apply(StoreEvent event);
}
