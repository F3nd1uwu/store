package ru.web.store.order.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.web.store.order.application.dto.CreateOrderCommand;
import ru.web.store.order.domain.model.Money;
import ru.web.store.order.domain.model.Order;
import ru.web.store.order.domain.model.OrderItem;
import ru.web.store.order.domain.repository.OrderRepository;
import ru.web.store.order.infrastructure.persistence.SpringDataOutboxRepository;
import ru.web.store.order.infrastructure.persistence.entity.OutboxEventJpaEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final SpringDataOutboxRepository outboxRepository;

    public CreateOrderUseCase(OrderRepository orderRepository, SpringDataOutboxRepository outboxRepository) {
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public Order execute(CreateOrderCommand command) {
        // 1. Создаем доменный агрегат заказа
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

        // 2. Сохраняем заказ в таблицу 'orders'
        Order savedOrder = orderRepository.save(order);

        // 3. Формируем JSON события для брокера Kafka
        String jsonPayload = String.format(
                "{\"orderId\":\"%s\",\"customerId\":\"%s\",\"totalAmount\":%s,\"currency\":\"%s\",\"status\":\"%s\"}",
                savedOrder.getId(),
                savedOrder.getCustomerId(),
                savedOrder.getTotalAmount().amount(),
                savedOrder.getTotalAmount().currency(),
                savedOrder.getStatus()
        );

        // 4. В ЭТОЙ ЖЕ ТРАНЗАКЦИИ сохраняем событие в 'outbox_events' со статусом PENDING
        OutboxEventJpaEntity outboxEvent = new OutboxEventJpaEntity(
                UUID.randomUUID(),
                "ORDER",
                savedOrder.getId(),
                "ORDER_CREATED",
                jsonPayload,
                "PENDING",
                Instant.now()
        );
        outboxRepository.save(outboxEvent);

        return savedOrder;
    }
}