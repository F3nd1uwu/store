package ru.web.store.contracts;

import com.google.protobuf.StringValue;
import io.grpc.*;
import io.grpc.protobuf.ProtoUtils;

public final class CatalogRpc {
  private CatalogRpc() {}

  public static final MethodDescriptor<StringValue, StringValue> QUOTE =
      MethodDescriptor.<StringValue, StringValue>newBuilder()
          .setType(MethodDescriptor.MethodType.UNARY)
          .setFullMethodName("store.Catalog/Quote")
          .setRequestMarshaller(ProtoUtils.marshaller(StringValue.getDefaultInstance()))
          .setResponseMarshaller(ProtoUtils.marshaller(StringValue.getDefaultInstance()))
          .build();
}
