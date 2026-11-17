package ru.web.store.inventory.infrastructure;

import com.google.protobuf.StringValue;
import io.grpc.*;
import io.grpc.stub.ServerCalls;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import ru.web.store.contracts.*;
import ru.web.store.inventory.domain.ProductRepository;

@Component
public class CatalogGrpcServer {
  private final ProductRepository products;
  private Server server;

  public CatalogGrpcServer(ProductRepository products) {
    this.products = products;
  }

  @PostConstruct
  public void start() throws Exception {
    server =
        ServerBuilder.forPort(9093)
            .addService(
                ServerServiceDefinition.builder("store.Catalog")
                    .addMethod(
                        CatalogRpc.QUOTE,
                        ServerCalls.asyncUnaryCall(
                            (request, observer) -> {
                              try {
                                var product =
                                    products.find(java.util.UUID.fromString(request.getValue()));
                                if (product.isEmpty()) {
                                  observer.onError(Status.NOT_FOUND.asRuntimeException());
                                  return;
                                }
                                observer.onNext(StringValue.of(Json.write(product.get())));
                                observer.onCompleted();
                              } catch (Exception e) {
                                observer.onError(Status.INVALID_ARGUMENT.asRuntimeException());
                              }
                            }))
                    .build())
            .build()
            .start();
  }

  @PreDestroy
  public void stop() {
    if (server != null) server.shutdown();
  }
}
