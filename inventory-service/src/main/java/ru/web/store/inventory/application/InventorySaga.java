package ru.web.store.inventory.application;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.web.store.contracts.StoreEvent;

@Service
public class InventorySaga {
  private final SagaStore store;

  public InventorySaga(SagaStore store) {
    this.store = store;
  }

  @Transactional
  @CacheEvict(value = "products", allEntries = true)
  public void handle(StoreEvent event) {
    if (!store.begin(event)) return;
    switch (event.type()) {
      case "ORDER_CREATED" -> {
        boolean ok = store.reserve(event);
        store.emit(
            event.next(ok ? "STOCK_RESERVED" : "ORDER_REJECTED", ok ? null : "Insufficient stock"));
      }
      case "STOCK_RESERVED" -> {
        boolean ok = store.charge(event);
        store.emit(
            event.next(
                ok ? "PAYMENT_SUCCEEDED" : "PAYMENT_FAILED",
                ok ? null : "Insufficient balance or currency mismatch"));
      }
      case "PAYMENT_SUCCEEDED" -> {
        store.finish(event, true);
        store.emit(event.next("ORDER_CONFIRMED", null));
      }
      case "PAYMENT_FAILED" -> {
        store.finish(event, false);
        store.emit(event.next("ORDER_REJECTED", event.reason()));
      }
      default -> {}
    }
  }
}
