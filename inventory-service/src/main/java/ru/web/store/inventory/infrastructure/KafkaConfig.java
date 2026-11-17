package ru.web.store.inventory.infrastructure;

import java.util.*;
import org.apache.kafka.common.serialization.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.*;

@Configuration
@EnableKafka
public class KafkaConfig {
  private final String bootstrap;

  public KafkaConfig(@Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrap) {
    this.bootstrap = bootstrap;
  }

  @Bean
  KafkaAdmin kafkaAdmin() {
    return new KafkaAdmin(Map.of("bootstrap.servers", bootstrap));
  }

  @Bean
  KafkaTemplate<String, String> kafkaTemplate() {
    Map<String, Object> props = new HashMap<>();
    props.put("bootstrap.servers", bootstrap);
    props.put("key.serializer", StringSerializer.class);
    props.put("value.serializer", StringSerializer.class);
    props.put("acks", "all");
    props.put("enable.idempotence", true);
    return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
  }

  @Bean
  ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
      DefaultErrorHandler errorHandler) {
    Map<String, Object> props = new HashMap<>();
    props.put("bootstrap.servers", bootstrap);
    props.put("group.id", "inventory-saga-v1");
    props.put("auto.offset.reset", "earliest");
    props.put("enable.auto.commit", false);
    props.put("key.deserializer", StringDeserializer.class);
    props.put("value.deserializer", StringDeserializer.class);
    var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
    factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props));
    factory.setCommonErrorHandler(errorHandler);
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
    return factory;
  }
}
