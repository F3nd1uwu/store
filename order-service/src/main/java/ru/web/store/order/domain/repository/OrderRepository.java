package ru.web.store.order.domain.repository;

import java.util.Optional;
import java.util.UUID;
import ru.web.store.order.domain.model.Order;

public interface OrderRepository {
  Order save(Order order);

  Optional<Order> findById(UUID id);
}
