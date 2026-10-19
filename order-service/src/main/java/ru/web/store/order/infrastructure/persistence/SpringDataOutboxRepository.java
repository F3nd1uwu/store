package ru.web.store.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.web.store.order.infrastructure.persistence.entity.OutboxEventJpaEntity;

import java.util.List;
import java.util.UUID;

public interface SpringDataOutboxRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {
    List<OutboxEventJpaEntity> findTop10ByStatusOrderByCreatedAtAsc(String status);
}