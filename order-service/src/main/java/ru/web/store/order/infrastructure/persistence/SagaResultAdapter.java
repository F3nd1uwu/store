package ru.web.store.order.infrastructure.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.web.store.contracts.StoreEvent;
import ru.web.store.order.application.port.SagaResultStore;

@Repository
public class SagaResultAdapter implements SagaResultStore {
  private final JdbcTemplate jdbc;

  public SagaResultAdapter(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public void apply(StoreEvent e) {

    if (jdbc.update(
            "INSERT INTO order_inbox(event_id) VALUES(?) ON CONFLICT DO NOTHING", e.eventId())
        == 0) return;
    if (!e.type().equals("ORDER_CONFIRMED") && !e.type().equals("ORDER_REJECTED")) return;
    jdbc.update(
        "UPDATE orders SET status=? WHERE id=? AND status='NEW'",
        e.type().equals("ORDER_CONFIRMED") ? "PAID" : "CANCELLED",
        e.orderId());
    jdbc.update(
        "INSERT INTO order_sagas(order_id,state,reason) VALUES(?,?,?) ON CONFLICT(order_id) DO"
            + " NOTHING",
        e.orderId(),
        e.type(),
        e.reason());
  }
}
