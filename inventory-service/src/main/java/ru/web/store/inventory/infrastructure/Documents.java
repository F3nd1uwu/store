package ru.web.store.inventory.infrastructure;

import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.web.store.inventory.application.DocumentStore;

@Repository
public class Documents implements DocumentStore {
  private final JdbcTemplate jdbc;

  public Documents(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public void save(String kind, UUID id, UUID owner, String payload) {
    int changed =
        jdbc.update(
            "INSERT INTO documents(kind,id,owner_id,payload) VALUES(?,?,?,?) ON CONFLICT(kind,id)"
                + " DO UPDATE SET payload=excluded.payload WHERE"
                + " documents.owner_id=excluded.owner_id",
            kind,
            id,
            owner,
            payload);
    if (changed != 1) throw new IllegalArgumentException("Document belongs to another user");
  }

  public Optional<String> find(String kind, UUID id, UUID owner) {
    return jdbc
        .query(
            "SELECT payload FROM documents WHERE kind=? AND id=? AND owner_id=?",
            (rs, n) -> rs.getString(1),
            kind,
            id,
            owner)
        .stream()
        .findFirst();
  }
}
