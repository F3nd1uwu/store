package ru.web.store.inventory.application;

import java.util.UUID;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.web.store.inventory.domain.*;

@Service
public class CatalogService {
  private final ProductRepository products;

  public CatalogService(ProductRepository products) {
    this.products = products;
  }

  @CachePut(value = "products", key = "#result.id()")
  public Product create(Product product) {
    return products.save(product);
  }

  @Cacheable(value = "products", key = "#id", unless = "#result == null")
  public Product get(UUID id) {
    return products.find(id).orElse(null);
  }
}
