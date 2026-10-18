package ru.web.store.order.infrastructure.persistence;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.web.store.order.domain.model.Money;
import ru.web.store.order.domain.model.Order;
import ru.web.store.order.domain.model.OrderItem;
import ru.web.store.order.domain.model.OrderStatus;
import ru.web.store.order.domain.repository.OrderRepository;
import ru.web.store.order.infrastructure.persistence.entity.OrderJpaEntity;
import ru.web.store.order.infrastructure.persistence.entity.OrderItemJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Primary
public class PostgresOrderRepository implements OrderRepository {

    private final SpringDataOrderRepository springDataRepository;

    public PostgresOrderRepository(SpringDataOrderRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity(
                order.getId(),
                order.getCustomerId(),
                order.getStatus().name(),
                order.getTotalAmount().amount(),
                order.getTotalAmount().currency(),
                order.getCreatedAt()
        );

        for (OrderItem item : order.getItems()) {
            OrderItemJpaEntity itemEntity = new OrderItemJpaEntity(
                    item.getId(),
                    entity,
                    item.getProductId(),
                    item.getQuantity(),
                    item.getUnitPrice().amount(),
                    item.getUnitPrice().currency()
            );
            entity.addItem(itemEntity);
        }

        springDataRepository.save(entity);
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(UUID id) {
        return springDataRepository.findById(id).map(this::toDomain);
    }

    private Order toDomain(OrderJpaEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
                .map(i -> new OrderItem(
                        i.getId(),
                        i.getProductId(),
                        i.getQuantity(),
                        Money.of(i.getUnitPrice(), i.getCurrency())
                ))
                .toList();

        return new Order(
                entity.getId(),
                entity.getCustomerId(),
                items,
                entity.getCurrency()
        );
    }
}