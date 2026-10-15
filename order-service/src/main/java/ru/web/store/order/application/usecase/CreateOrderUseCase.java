package ru.web.store.order.application.usecase;

import org.springframework.stereotype.Service;
import ru.web.store.order.application.dto.CreateOrderCommand;
import ru.web.store.order.domain.model.Money;
import ru.web.store.order.domain.model.Order;
import ru.web.store.order.domain.model.OrderItem;
import ru.web.store.order.domain.repository.OrderRepository;

import java.util.List;
import java.util.UUID;

@Service
public class CreateOrderUseCase {

    private final OrderRepository orderRepository;

    public CreateOrderUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order execute(CreateOrderCommand command) {
        List<OrderItem> orderItems = command.items().stream()
                .map(item -> new OrderItem(
                        UUID.randomUUID(),
                        item.productId(),
                        item.quantity(),
                        Money.of(item.unitPrice(), command.currency())
                ))
                .toList();

        Order order = new Order(
                UUID.randomUUID(),
                command.customerId(),
                orderItems,
                command.currency()
        );

        return orderRepository.save(order);
    }
}