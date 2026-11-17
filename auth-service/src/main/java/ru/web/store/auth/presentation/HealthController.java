package ru.web.store.auth.presentation;

import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
public class HealthController {
  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of("status", "UP", "service", "auth-service");
  }
}
