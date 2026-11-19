package ru.web.store.inventory.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ru.web.store.inventory.application.CatalogService;
import ru.web.store.inventory.domain.Product;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
  private final CatalogService catalog;

  public ProductController(CatalogService catalog) {
    this.catalog = catalog;
  }

  public record CreateProduct(
      @NotBlank @Size(max = 200) String title,
      @NotNull @DecimalMin("0") BigDecimal price,
      @NotNull @Pattern(regexp = "[A-Z]{3}") String currency,
      @Min(0) int quantity) {}

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Product create(@Valid @RequestBody CreateProduct p) {
    return catalog.create(
        new Product(UUID.randomUUID(), p.title(), p.price(), p.currency(), p.quantity()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<Product> get(@PathVariable UUID id) {
    return ResponseEntity.ofNullable(catalog.get(id));
  }
}
