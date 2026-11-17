package ru.web.store.order.application;

import java.util.*;
import org.springframework.stereotype.Service;
import ru.web.store.contracts.Json;
import ru.web.store.order.domain.model.*;

@Service
public class ShoppingService {
  private final DocumentStore store;

  public ShoppingService(DocumentStore store) {
    this.store = store;
  }

  public Cart cart(UUID user, List<Cart.Line> lines) {
    Cart cart = new Cart(user, lines);
    store.save("cart", user, user, Json.write(cart));
    return cart;
  }

  public Optional<Cart> cart(UUID user) {
    return store.find("cart", user, user).map(s -> Json.read(s, Cart.class));
  }

  public Coupon coupon(UUID user, Coupon coupon) {
    store.save("coupon", coupon.id(), user, Json.write(coupon));
    return coupon;
  }

  public Optional<Coupon> coupon(UUID user, UUID id) {
    return store.find("coupon", id, user).map(s -> Json.read(s, Coupon.class));
  }
}
