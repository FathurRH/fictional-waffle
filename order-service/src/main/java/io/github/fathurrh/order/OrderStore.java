package io.github.fathurrh.order;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** In-memory storage for step 1. A real database (and the outbox pattern) comes later. */
@Component
public class OrderStore {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public void save(Order order) {
        orders.put(order.id(), order);
    }

    public Optional<Order> find(String id) {
        return Optional.ofNullable(orders.get(id));
    }
}
