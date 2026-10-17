package io.github.fathurrh.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedListener {

    static final String ORDERS = "orders";
    static final String PAYMENTS = "payments";

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedListener.class);

    private final PaymentProcessor processor;
    private final KafkaTemplate<String, Object> kafka;

    public OrderCreatedListener(PaymentProcessor processor, KafkaTemplate<String, Object> kafka) {
        this.processor = processor;
        this.kafka = kafka;
    }

    @KafkaListener(topics = ORDERS)
    public void onOrderCreated(OrderCreatedEvent order) {
        PaymentProcessedEvent result = processor.charge(order);
        log.info("Payment for order {}: success={} ({})", order.orderId(), result.success(), result.reason());
        kafka.send(PAYMENTS, order.orderId(), result);
    }
}
