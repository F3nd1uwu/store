package ru.web.store.order.application.usecase;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.web.store.contracts.StoreEvent;
import ru.web.store.order.application.port.SagaResultStore;

@Service
public class ApplySagaResult {
  private final SagaResultStore store;

  public ApplySagaResult(SagaResultStore store) {
    this.store = store;
  }

  @Transactional
  @CacheEvict(value = "orders", allEntries = true)
  public void execute(StoreEvent event) {
    store.apply(event);
  }
}
