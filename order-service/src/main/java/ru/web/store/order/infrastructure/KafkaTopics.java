package ru.web.store.order.infrastructure;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.*;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopics {
  @Bean
  NewTopic orderEvents() {
    return TopicBuilder.name("store.order.v1").partitions(1).replicas(1).build();
  }

  @Bean
  NewTopic inventoryEvents() {
    return TopicBuilder.name("store.inventory.v1").partitions(1).replicas(1).build();
  }
}
