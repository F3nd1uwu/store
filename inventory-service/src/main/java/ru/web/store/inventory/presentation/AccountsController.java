package ru.web.store.inventory.presentation;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.web.store.inventory.application.*;
import ru.web.store.inventory.domain.*;

@RestController
@RequestMapping("/api/v1")
public class AccountsController {
  private final WalletStore wallets;
  private final CategoryService categories;

  public AccountsController(WalletStore wallets, CategoryService categories) {
    this.wallets = wallets;
    this.categories = categories;
  }

  public record Deposit(String currency, BigDecimal amount) {}

  @PostMapping("/wallet/deposit")
  public Wallet deposit(@AuthenticationPrincipal Jwt jwt, @RequestBody Deposit request) {
    return wallets.deposit(
        new Wallet(UUID.fromString(jwt.getSubject()), request.currency(), request.amount()));
  }

  @GetMapping("/wallet")
  public Wallet wallet(@AuthenticationPrincipal Jwt jwt) {
    return wallets.get(UUID.fromString(jwt.getSubject()));
  }

  @PostMapping("/categories")
  public Category category(@AuthenticationPrincipal Jwt jwt, @RequestBody Category category) {
    return categories.save(UUID.fromString(jwt.getSubject()), category);
  }

  @GetMapping("/categories/{id}")
  public ResponseEntity<Category> category(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    return ResponseEntity.of(categories.get(UUID.fromString(jwt.getSubject()), id));
  }
}
