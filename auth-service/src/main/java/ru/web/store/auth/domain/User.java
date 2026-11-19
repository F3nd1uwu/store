package ru.web.store.auth.domain;

import java.util.UUID;

public record User(UUID id, String email, String passwordHash, String role, boolean active) {}
