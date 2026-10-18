package ru.web.store.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.web.store.order.infrastructure.persistence.entity.OrderJpaEntity;

import java.util.UUID;

public interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, UUID> {
}