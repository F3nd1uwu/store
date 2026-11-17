package ru.web.store.inventory.application;

import java.util.*;
import org.springframework.stereotype.Service;
import ru.web.store.contracts.Json;
import ru.web.store.inventory.domain.Category;

@Service
public class CategoryService {
  private final DocumentStore store;

  public CategoryService(DocumentStore store) {
    this.store = store;
  }

  public Category save(UUID owner, Category category) {
    store.save("category", category.id(), owner, Json.write(category));
    return category;
  }

  public Optional<Category> get(UUID owner, UUID id) {
    return store.find("category", id, owner).map(s -> Json.read(s, Category.class));
  }
}
