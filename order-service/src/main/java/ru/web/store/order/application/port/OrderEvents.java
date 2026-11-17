package ru.web.store.order.application.port;

import ru.web.store.contracts.StoreEvent;

public interface OrderEvents {
  void append(StoreEvent event);
}
