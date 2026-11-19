package ru.web.store.auth.infrastructure.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class UserEntity {
  @Id private UUID id;

  @Column(nullable = false)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(nullable = false)
  private String role;

  @Column(nullable = false)
  private boolean active;

  protected UserEntity() {}

  public UserEntity(UUID id, String email, String passwordHash, String role, boolean active) {
    this.id = id;
    this.email = email;
    this.passwordHash = passwordHash;
    this.role = role;
    this.active = active;
  }

  public UUID getId() {
    return id;
  }

  public String getRole() {
    return role;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public boolean isActive() {
    return active;
  }
}
