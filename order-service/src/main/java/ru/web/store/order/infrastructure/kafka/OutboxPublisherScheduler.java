package ru.web.store.order.infrastructure.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.web.store.order.infrastructure.persistence.SpringDataOutboxRepository;
import ru.web.store.order.infrastructure.persistence.entity.OutboxEventJpaEntity;

import java.util.List;

@Component
public class OutboxPublisherScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherScheduler.class);
    private static final String TOPIC_NAME = "order-events";

    private final SpringDataOutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisherScheduler(SpringDataOutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEventJpaEntity> pendingEvents = outboxRepository.findTop10ByStatusOrderByCreatedAtAsc("PENDING");

        for (OutboxEventJpaEntity event : pendingEvents) {
            try {
                log.info("-----> [OUTBOX] Отправляем событие {} в Kafka топик '{}'", event.getId(), TOPIC_NAME);

                // Отправляем в Kafka (ключ — ID заказа, значение — JSON)
                kafkaTemplate.send(TOPIC_NAME, event.getAggregateId().toString(), event.getPayload());

                // Меняем статус на SENT
                event.setStatus("SENT");
                outboxRepository.save(event);

                log.info("-----> [OUTBOX] Событие {} успешно отправлено в Kafka!", event.getId());
            } catch (Exception e) {
                log.error("Ошибка при отправке события в Kafka: {}", e.getMessage());
            }
        }
    }
}