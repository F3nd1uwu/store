package ru.web.store.order.infrastructure.grpc;

import com.google.protobuf.StringValue;
import io.grpc.*;
import io.grpc.stub.ClientCalls;
import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.web.store.contracts.*;
import ru.web.store.order.application.port.CatalogPort;

@Component
public class CatalogGrpcClient implements CatalogPort {
  private final ManagedChannel channel;

  public CatalogGrpcClient(@Value("${catalog.grpc.host:localhost}") String host) {
    channel = ManagedChannelBuilder.forAddress(host, 9093).usePlaintext().build();
  }

  private record Product(UUID id, String title, BigDecimal price, String currency, int quantity) {}

  public Quote quote(UUID id) {
    var reply =
        ClientCalls.blockingUnaryCall(
            channel,
            CatalogRpc.QUOTE,
            CallOptions.DEFAULT.withDeadlineAfter(3, TimeUnit.SECONDS),
            StringValue.of(id.toString()));
    var p = Json.read(reply.getValue(), Product.class);
    return new Quote(p.price(), p.currency(), p.quantity());
  }

  @PreDestroy
  public void close() {
    channel.shutdown();
  }
}
