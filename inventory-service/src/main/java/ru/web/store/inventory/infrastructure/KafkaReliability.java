package ru.web.store.inventory.infrastructure;

import org.springframework.context.annotation.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaReliability {
  @Bean
  DefaultErrorHandler kafkaErrorHandler() {
    return new DefaultErrorHandler(new FixedBackOff(2000L, FixedBackOff.UNLIMITED_ATTEMPTS));
  }
}
