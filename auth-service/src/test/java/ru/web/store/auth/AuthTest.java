package ru.web.store.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.web.store.auth.application.AuthUseCase;
import ru.web.store.auth.application.TokenIssuer;
import ru.web.store.auth.domain.UserRepository;

class AuthTest {
  @Test
  void duplicateEmailCannotCreateAccount() {
    UserRepository users = mock(UserRepository.class);
    when(users.existsByEmail("a@example.com")).thenReturn(true);
    AuthUseCase auth = new AuthUseCase(users, mock(PasswordEncoder.class), mock(TokenIssuer.class));
    assertThrows(IllegalArgumentException.class, () -> auth.register("A@example.com", "password"));
    verify(users, never()).save(any());
  }

  @Test
  void unknownAccountCannotLogin() {
    AuthUseCase auth =
        new AuthUseCase(
            mock(UserRepository.class), mock(PasswordEncoder.class), mock(TokenIssuer.class));
    assertThrows(
        org.springframework.security.authentication.BadCredentialsException.class,
        () -> auth.login("missing@example.com", "password"));
  }

  @Test
  void wrongPasswordCannotIssueToken() {
    var users = mock(UserRepository.class);
    var encoder = mock(PasswordEncoder.class);
    var tokens = mock(TokenIssuer.class);
    when(users.findByEmail("a@example.com"))
        .thenReturn(
            java.util.Optional.of(
                new ru.web.store.auth.domain.User(
                    java.util.UUID.randomUUID(), "a@example.com", "hash", "CUSTOMER", true)));
    when(encoder.matches("wrong", "hash")).thenReturn(false);
    assertThrows(
        org.springframework.security.authentication.BadCredentialsException.class,
        () -> new AuthUseCase(users, encoder, tokens).login("a@example.com", "wrong"));
    verify(tokens, never()).issue(any(), any());
  }

  @Test
  void registrationStoresHashInsteadOfPassword() {
    var users = mock(UserRepository.class);
    var encoder = mock(PasswordEncoder.class);
    var tokens = mock(TokenIssuer.class);
    when(encoder.encode("password123")).thenReturn("bcrypt-hash");
    when(users.save(any())).thenAnswer(i -> i.getArgument(0));
    new AuthUseCase(users, encoder, tokens).register("A@example.com", "password123");
    verify(users)
        .save(
            argThat(
                user ->
                    user.email().equals("a@example.com")
                        && user.passwordHash().equals("bcrypt-hash")));
  }
}
