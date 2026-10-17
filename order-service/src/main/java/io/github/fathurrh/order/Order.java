package io.github.fathurrh.order;

import java.math.BigDecimal;

public record Order(String id, String customerId, BigDecimal amount, OrderStatus status) {

    public Order withStatus(OrderStatus newStatus) {
        return new Order(id, customerId, amount, newStatus);
    }
}
