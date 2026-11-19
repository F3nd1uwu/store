package ru.web.store.auth.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.web.store.auth.application.AuthUseCase;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthUseCase auth;

  public AuthController(AuthUseCase auth) {
    this.auth = auth;
  }

  public record Credentials(
      @Email @NotBlank String email, @NotBlank @Size(min = 8) String password) {}

  public record TokenResponse(String accessToken, String tokenType) {}

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public TokenResponse register(@Valid @RequestBody Credentials request) {
    return new TokenResponse(auth.register(request.email(), request.password()), "Bearer");
  }

  @PostMapping("/login")
  public TokenResponse login(@Valid @RequestBody Credentials request) {
    return new TokenResponse(auth.login(request.email(), request.password()), "Bearer");
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public String invalid(IllegalArgumentException e) {
    return e.getMessage();
  }
}
