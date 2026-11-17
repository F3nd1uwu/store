package ru.web.store.inventory.presentation;

import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiErrors {
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> invalid(IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
  }

  @ExceptionHandler(java.util.NoSuchElementException.class)
  public ResponseEntity<?> missing() {
    return ResponseEntity.status(404).body(Map.of("error", "Not found"));
  }

  @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
  public ResponseEntity<?> conflict() {
    return ResponseEntity.status(409).body(Map.of("error", "Data conflict"));
  }
}
