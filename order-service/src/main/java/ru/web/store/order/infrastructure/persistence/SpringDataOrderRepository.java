package ru.web.store.order.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.web.store.order.infrastructure.persistence.entity.OrderJpaEntity;

public interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, UUID> {}
