package ru.web.store.order.presentation.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.web.store.order.application.dto.CreateOrderCommand;
import ru.web.store.order.application.usecase.CreateOrderUseCase;
import ru.web.store.order.application.usecase.GetOrderUseCase;
import ru.web.store.order.domain.model.Order;
import ru.web.store.order.presentation.request.CreateOrderRequest;
import ru.web.store.order.presentation.response.OrderResponse;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

  private final CreateOrderUseCase createOrderUseCase;
  private final GetOrderUseCase getOrderUseCase;

  public OrderController(CreateOrderUseCase createOrderUseCase, GetOrderUseCase getOrderUseCase) {
    this.createOrderUseCase = createOrderUseCase;
    this.getOrderUseCase = getOrderUseCase;
  }

  @PostMapping
  public ResponseEntity<OrderResponse> createOrder(
      @Valid @RequestBody CreateOrderRequest request, @AuthenticationPrincipal Jwt jwt) {
    CreateOrderCommand command =
        new CreateOrderCommand(
            UUID.fromString(jwt.getSubject()),
            request.currency(),
            request.items().stream()
                .map(
                    i -> new CreateOrderCommand.ItemCommand(i.productId(), i.quantity(), i.price()))
                .toList());

    Order order = createOrderUseCase.execute(command);
    return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.fromDomain(order));
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderResponse> getOrderById(
      @PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
    Order order = getOrderUseCase.execute(id);
    if (order == null || !order.getCustomerId().toString().equals(jwt.getSubject())) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(OrderResponse.fromDomain(order));
  }
}
