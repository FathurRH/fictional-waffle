package io.github.fathurrh.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentResultListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentResultListener.class);

    private final OrderStore store;

    public PaymentResultListener(OrderStore store) {
        this.store = store;
    }

    @KafkaListener(topics = Topics.PAYMENTS)
    public void onPaymentProcessed(PaymentProcessedEvent event) {
        store.find(event.orderId()).ifPresentOrElse(order -> {
            OrderStatus status = event.success() ? OrderStatus.PAID : OrderStatus.PAYMENT_FAILED;
            store.save(order.withStatus(status));
            log.info("Order {} is now {} ({})", order.id(), status, event.reason());
        }, () -> log.warn("Payment result for unknown order {}", event.orderId()));
    }
}
