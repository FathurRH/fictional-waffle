package io.github.fathurrh.order;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    public record CreateOrderRequest(String customerId, BigDecimal amount) {
    }

    private final OrderStore store;
    private final KafkaTemplate<String, Object> kafka;

    public OrderController(OrderStore store, KafkaTemplate<String, Object> kafka) {
        this.store = store;
        this.kafka = kafka;
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody CreateOrderRequest request) {
        Order order = new Order(UUID.randomUUID().toString(), request.customerId(), request.amount(),
                OrderStatus.PENDING_PAYMENT);
        store.save(order);

        // The order id is the message key, so every event for one order lands on the same
        // partition and is read in order.
        kafka.send(Topics.ORDERS, order.id(),
                new OrderCreatedEvent(order.id(), order.customerId(), order.amount()));

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@PathVariable String id) {
        return store.find(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
