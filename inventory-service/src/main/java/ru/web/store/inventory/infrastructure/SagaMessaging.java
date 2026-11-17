package ru.web.store.inventory.infrastructure;

import java.util.concurrent.TimeUnit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.web.store.contracts.*;
import ru.web.store.inventory.application.InventorySaga;

@Component
public class SagaMessaging {
  private final InventorySaga saga;
  private final JdbcTemplate jdbc;
  private final KafkaTemplate<String, String> kafka;

  public SagaMessaging(InventorySaga saga, JdbcTemplate jdbc, KafkaTemplate<String, String> kafka) {
    this.saga = saga;
    this.jdbc = jdbc;
    this.kafka = kafka;
  }

  @KafkaListener(topics = {"store.order.v1", "store.inventory.v1"})
  public void receive(String payload) {
    saga.handle(Json.read(payload, StoreEvent.class));
  }

  @Scheduled(fixedDelay = 1000)
  @Transactional
  public void publish() throws Exception {
    var rows =
        jdbc.queryForList(
            "SELECT * FROM inventory_outbox WHERE sent=false ORDER BY created_at LIMIT 20 FOR"
                + " UPDATE SKIP LOCKED");
    for (var row : rows) {
      kafka
          .send("store.inventory.v1", row.get("order_id").toString(), row.get("payload").toString())
          .get(10, TimeUnit.SECONDS);
      jdbc.update("UPDATE inventory_outbox SET sent=true WHERE event_id=?", row.get("event_id"));
    }
  }
}
