package ru.web.store.inventory.domain;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
  Product save(Product product);

  Optional<Product> find(UUID id);
}
