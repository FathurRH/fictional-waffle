package io.github.fathurrh.order;

import java.math.BigDecimal;

/** Published to the "orders" topic when a customer places an order. */
public record OrderCreatedEvent(String orderId, String customerId, BigDecimal amount) {
}
