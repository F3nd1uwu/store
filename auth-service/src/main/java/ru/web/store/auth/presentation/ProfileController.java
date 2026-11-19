package ru.web.store.auth.presentation;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.web.store.auth.application.ProfileService;
import ru.web.store.auth.domain.CustomerProfile;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {
  private final ProfileService service;

  public ProfileController(ProfileService service) {
    this.service = service;
  }

  public record Request(String name, String address, String phone) {}

  @PutMapping
  public CustomerProfile save(@AuthenticationPrincipal Jwt jwt, @RequestBody Request r) {
    return service.save(
        new CustomerProfile(UUID.fromString(jwt.getSubject()), r.name(), r.address(), r.phone()));
  }

  @GetMapping
  public ResponseEntity<CustomerProfile> get(@AuthenticationPrincipal Jwt jwt) {
    return ResponseEntity.of(service.get(UUID.fromString(jwt.getSubject())));
  }
}
