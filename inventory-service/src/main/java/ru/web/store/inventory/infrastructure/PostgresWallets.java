package ru.web.store.inventory.infrastructure;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.web.store.inventory.application.WalletStore;
import ru.web.store.inventory.domain.Wallet;

@Repository
public class PostgresWallets implements WalletStore {
  private final JdbcTemplate jdbc;

  public PostgresWallets(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Transactional
  public Wallet deposit(Wallet w) {
    int changed =
        jdbc.update(
            "INSERT INTO wallets(customer_id,currency,balance) VALUES(?,?,?) ON"
                + " CONFLICT(customer_id) DO UPDATE SET balance=wallets.balance+excluded.balance"
                + " WHERE wallets.currency=excluded.currency",
            w.customerId(),
            w.currency(),
            w.balance());
    if (changed == 0) throw new IllegalArgumentException("Wallet currency mismatch");
    return get(w.customerId());
  }

  public Wallet get(UUID id) {
    return jdbc
        .query(
            "SELECT * FROM wallets WHERE customer_id=?",
            (rs, n) -> new Wallet(id, rs.getString("currency"), rs.getBigDecimal("balance")),
            id)
        .stream()
        .findFirst()
        .orElseThrow();
  }
}
