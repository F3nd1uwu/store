package ru.web.store.auth.application;

import java.util.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.web.store.auth.domain.*;

@Service
public class AuthUseCase {
  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final TokenIssuer jwt;

  public AuthUseCase(UserRepository users, PasswordEncoder encoder, TokenIssuer jwt) {
    this.users = users;
    this.encoder = encoder;
    this.jwt = jwt;
  }

  @Transactional
  public String register(String email, String password) {
    String normalized = email.trim().toLowerCase(Locale.ROOT);
    if (users.existsByEmail(normalized))
      throw new IllegalArgumentException("Email already registered");
    User user =
        users.save(
            new User(UUID.randomUUID(), normalized, encoder.encode(password), "CUSTOMER", true));
    return jwt.issue(user.id(), user.role());
  }

  @Transactional(readOnly = true)
  public String login(String email, String password) {
    User user =
        users
            .findByEmail(email.trim().toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    if (!user.active() || !encoder.matches(password, user.passwordHash()))
      throw new BadCredentialsException("Invalid credentials");
    return jwt.issue(user.id(), user.role());
  }
}
