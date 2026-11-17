package ru.web.store.auth.infrastructure.persistence;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import ru.web.store.auth.domain.*;

@Repository
public class UserRepositoryAdapter implements UserRepository {
  private final UserJpaRepository repository;

  public UserRepositoryAdapter(UserJpaRepository repository) {
    this.repository = repository;
  }

  public boolean existsByEmail(String email) {
    return repository.existsByEmail(email);
  }

  public User save(User user) {
    repository.save(
        new UserEntity(user.id(), user.email(), user.passwordHash(), user.role(), user.active()));
    return user;
  }

  public Optional<User> findByEmail(String email) {
    return repository
        .findByEmail(email)
        .map(u -> new User(u.getId(), email, u.getPasswordHash(), u.getRole(), u.isActive()));
  }
}
