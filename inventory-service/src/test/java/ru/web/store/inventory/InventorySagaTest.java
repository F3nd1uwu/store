package ru.web.store.inventory;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import ru.web.store.contracts.StoreEvent;
import ru.web.store.inventory.application.*;

class InventorySagaTest {
  private StoreEvent event(String type) {
    return new StoreEvent(
        UUID.randomUUID(),
        type,
        UUID.randomUUID(),
        UUID.randomUUID(),
        BigDecimal.TEN,
        "RUB",
        List.of(new StoreEvent.Line(UUID.randomUUID(), 1)),
        null);
  }

  @Test
  void duplicateDeliveryDoesNotRepeatBusinessOperation() {
    var store = mock(SagaStore.class);
    var e = event("ORDER_CREATED");
    new InventorySaga(store).handle(e);
    verify(store, never()).reserve(any());
    verify(store, never()).emit(any());
  }

  @Test
  void insufficientStockProducesRejection() {
    var store = mock(SagaStore.class);
    var e = event("ORDER_CREATED");
    when(store.begin(e)).thenReturn(true);
    new InventorySaga(store).handle(e);
    verify(store).emit(argThat(reply -> reply.type().equals("ORDER_REJECTED")));
  }

  @Test
  void paymentFailureReleasesReservationBeforeRejection() {
    var store = mock(SagaStore.class);
    var e = event("PAYMENT_FAILED");
    when(store.begin(e)).thenReturn(true);
    new InventorySaga(store).handle(e);
    var sequence = inOrder(store);
    sequence.verify(store).finish(e, false);
    sequence.verify(store).emit(argThat(reply -> reply.type().equals("ORDER_REJECTED")));
  }

  @Test
  void successfulPaymentConfirmsReservation() {
    var store = mock(SagaStore.class);
    var e = event("PAYMENT_SUCCEEDED");
    when(store.begin(e)).thenReturn(true);
    new InventorySaga(store).handle(e);
    verify(store).finish(e, true);
    verify(store).emit(argThat(reply -> reply.type().equals("ORDER_CONFIRMED")));
  }
}
