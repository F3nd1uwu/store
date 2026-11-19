package ru.web.store.inventory.application;

import ru.web.store.contracts.StoreEvent;

public interface SagaStore {
  boolean begin(StoreEvent event);

  boolean reserve(StoreEvent event);

  boolean charge(StoreEvent event);

  void finish(StoreEvent event, boolean success);

  void emit(StoreEvent event);
}
