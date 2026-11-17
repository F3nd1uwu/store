package ru.web.store.inventory.infrastructure;

import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.web.store.contracts.*;
import ru.web.store.inventory.application.SagaStore;

@Repository
public class PostgresSagaStore implements SagaStore {
  private final JdbcTemplate jdbc;

  public PostgresSagaStore(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public boolean begin(StoreEvent e) {
    return jdbc.update(
            "INSERT INTO inbox_events(event_id) VALUES(?) ON CONFLICT DO NOTHING", e.eventId())
        == 1;
  }

  public void emit(StoreEvent e) {
    jdbc.update(
        "INSERT INTO inventory_outbox(event_id,order_id,payload) VALUES(?,?,?)",
        e.eventId(),
        e.orderId(),
        Json.write(e));
  }

  public boolean reserve(StoreEvent e) {
    if (!jdbc.queryForList("SELECT order_id FROM reservations WHERE order_id=?", e.orderId())
        .isEmpty()) return true;
    var quantities = new TreeMap<UUID, Integer>();
    for (var line : e.items()) quantities.merge(line.productId(), line.quantity(), Math::addExact);
    boolean enough = true;
    for (var entry : quantities.entrySet()) {
      var rows =
          jdbc.queryForList(
              "SELECT quantity,currency FROM products WHERE id=? FOR UPDATE", entry.getKey());
      if (rows.isEmpty()
          || ((Number) rows.getFirst().get("quantity")).intValue() < entry.getValue()
          || !e.currency().equals(rows.getFirst().get("currency"))) enough = false;
    }
    if (!enough) return false;
    for (var entry : quantities.entrySet())
      jdbc.update(
          "UPDATE products SET quantity=quantity-? WHERE id=?", entry.getValue(), entry.getKey());
    jdbc.update(
        "INSERT INTO reservations(order_id,customer_id,state,items) VALUES(?,?,'ACTIVE',?)",
        e.orderId(),
        e.customerId(),
        Json.write(e.items()));
    return true;
  }

  public boolean charge(StoreEvent e) {
    if (!jdbc.queryForList("SELECT order_id FROM payments WHERE order_id=?", e.orderId()).isEmpty())
      return true;
    var payment =
        new ru.web.store.inventory.domain.Payment(
            e.orderId(), e.customerId(), e.amount(), e.currency());
    int changed =
        jdbc.update(
            "UPDATE wallets SET balance=balance-? WHERE customer_id=? AND currency=? AND"
                + " balance>=?",
            e.amount(),
            e.customerId(),
            e.currency(),
            e.amount());
    if (changed == 0) return false;
    jdbc.update(
        "INSERT INTO payments(order_id,customer_id,amount,currency,state) VALUES(?,?,?,?,'PAID')",
        e.orderId(),
        e.customerId(),
        e.amount(),
        e.currency());
    return true;
  }

  public void finish(StoreEvent e, boolean success) {
    String state =
        new ru.web.store.inventory.domain.Reservation(
                e.orderId(), ru.web.store.inventory.domain.Reservation.State.ACTIVE)
            .finish(success)
            .state()
            .name();
    int changed =
        jdbc.update(
            "UPDATE reservations SET state=? WHERE order_id=? AND state='ACTIVE'",
            state,
            e.orderId());
    if (changed == 1 && !success)
      for (var line : e.items())
        jdbc.update(
            "UPDATE products SET quantity=quantity+? WHERE id=?",
            line.quantity(),
            line.productId());
  }
}
