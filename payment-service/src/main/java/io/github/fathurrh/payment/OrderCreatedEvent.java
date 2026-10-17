package io.github.fathurrh.payment;

import java.math.BigDecimal;

/** Consumed from the "orders" topic. Mirrors the event order-service publishes. */
public record OrderCreatedEvent(String orderId, String customerId, BigDecimal amount) {
}
