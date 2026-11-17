package ru.web.store.inventory.domain;

import java.util.UUID;

public record Category(UUID id, String title) {
  public Category {
    if (id == null || title == null || title.isBlank())
      throw new IllegalArgumentException("Category title required");
  }
}
