package ru.web.store.auth.domain;

import java.util.UUID;

public record CustomerProfile(UUID id, String name, String address, String phone) {
  public CustomerProfile {
    if (id == null || name == null || name.isBlank() || address == null || address.isBlank())
      throw new IllegalArgumentException("Name and address required");
  }
}
