package ru.web.store.inventory.infrastructure;

import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.web.store.inventory.domain.*;

@Repository
public class PostgresProducts implements ProductRepository {
  private final JdbcTemplate jdbc;

  public PostgresProducts(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Product save(Product p) {
    jdbc.update(
        "INSERT INTO products(id,title,price,currency,quantity) VALUES(?,?,?,?,?)",
        p.id(),
        p.title(),
        p.price(),
        p.currency(),
        p.quantity());
    return p;
  }

  public Optional<Product> find(UUID id) {
    return jdbc
        .query(
            "SELECT * FROM products WHERE id=?",
            (rs, n) ->
                new Product(
                    rs.getObject("id", UUID.class),
                    rs.getString("title"),
                    rs.getBigDecimal("price"),
                    rs.getString("currency"),
                    rs.getInt("quantity")),
            id)
        .stream()
        .findFirst();
  }
}
