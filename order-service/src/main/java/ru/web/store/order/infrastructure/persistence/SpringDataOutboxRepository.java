package ru.web.store.order.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.web.store.order.infrastructure.persistence.entity.OutboxEventJpaEntity;

public interface SpringDataOutboxRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  List<OutboxEventJpaEntity> findTop10ByStatusOrderByCreatedAtAsc(String status);
}
