package ru.web.store.auth.application;

import java.util.*;

public interface DocumentStore {
  void save(String kind, UUID id, UUID owner, String payload);

  Optional<String> find(String kind, UUID id, UUID owner);
}
