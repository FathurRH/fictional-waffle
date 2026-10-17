package io.github.fathurrh.payment;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/** Fake payment gateway: orders above the limit are declined, so you can see both paths. */
@Component
public class PaymentProcessor {

    static final BigDecimal LIMIT = new BigDecimal("1000");

    public PaymentProcessedEvent charge(OrderCreatedEvent order) {
        if (order.amount() == null || order.amount().signum() <= 0) {
            return new PaymentProcessedEvent(order.orderId(), false, "invalid amount");
        }
        if (order.amount().compareTo(LIMIT) > 0) {
            return new PaymentProcessedEvent(order.orderId(), false, "amount over limit");
        }
        return new PaymentProcessedEvent(order.orderId(), true, "charged");
    }
}
