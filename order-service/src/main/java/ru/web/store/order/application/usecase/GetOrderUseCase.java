package ru.web.store.order.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.web.store.order.domain.model.Order;
import ru.web.store.order.domain.repository.OrderRepository;

import java.util.UUID;

@Service
public class GetOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetOrderUseCase.class);
    private final OrderRepository orderRepository;

    public GetOrderUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Cacheable(value = "orders", key = "#id")
    public Order execute(UUID id) {
        log.info("-----> [CACHE MISS] Идем в репозиторий за заказом с ID: {}", id);
        return orderRepository.findById(id).orElse(null);
    }
}