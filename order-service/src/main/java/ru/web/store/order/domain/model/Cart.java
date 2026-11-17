package ru.web.store.order.domain.model;

import java.util.*;

public record Cart(UUID id, List<Line> items) {
  public record Line(UUID productId, int quantity) {
    public Line {
      if (productId == null || quantity <= 0)
        throw new IllegalArgumentException("Invalid cart line");
    }
  }

  public Cart {
    if (id == null || items == null) throw new IllegalArgumentException("Invalid cart");
    items = List.copyOf(items);
  }
}
