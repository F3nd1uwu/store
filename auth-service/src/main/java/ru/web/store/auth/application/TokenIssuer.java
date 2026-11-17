package ru.web.store.auth.application;

import java.util.UUID;

public interface TokenIssuer {
  String issue(UUID userId, String role);
}
