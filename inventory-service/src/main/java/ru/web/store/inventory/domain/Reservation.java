package ru.web.store.inventory.domain;

import java.util.UUID;

public record Reservation(UUID orderId, State state) {
  public enum State {
    ACTIVE,
    CONFIRMED,
    RELEASED
  }

  public Reservation finish(boolean paid) {
    if (state != State.ACTIVE) throw new IllegalStateException("Reservation already finalized");
    return new Reservation(orderId, paid ? State.CONFIRMED : State.RELEASED);
  }
}
