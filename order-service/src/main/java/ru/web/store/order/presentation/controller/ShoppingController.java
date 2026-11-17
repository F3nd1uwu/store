package ru.web.store.order.presentation.controller;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.web.store.order.application.ShoppingService;
import ru.web.store.order.domain.model.*;

@RestController
@RequestMapping("/api/v1")
public class ShoppingController {
  private final ShoppingService service;

  public ShoppingController(ShoppingService service) {
    this.service = service;
  }

  @PutMapping("/cart")
  public Cart cart(@AuthenticationPrincipal Jwt jwt, @RequestBody List<Cart.Line> lines) {
    return service.cart(UUID.fromString(jwt.getSubject()), lines);
  }

  @GetMapping("/cart")
  public ResponseEntity<Cart> cart(@AuthenticationPrincipal Jwt jwt) {
    return ResponseEntity.of(service.cart(UUID.fromString(jwt.getSubject())));
  }

  @PostMapping("/coupons")
  public Coupon coupon(@AuthenticationPrincipal Jwt jwt, @RequestBody Coupon coupon) {
    return service.coupon(UUID.fromString(jwt.getSubject()), coupon);
  }

  @GetMapping("/coupons/{id}/quote")
  public BigDecimal quote(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @RequestParam BigDecimal amount) {
    if (amount.signum() < 0) throw new IllegalArgumentException("Negative amount");
    return service.coupon(UUID.fromString(jwt.getSubject()), id).orElseThrow().apply(amount);
  }
}
