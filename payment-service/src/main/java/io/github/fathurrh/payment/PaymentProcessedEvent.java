package io.github.fathurrh.payment;

/** Published to the "payments" topic once a payment succeeds or fails. */
public record PaymentProcessedEvent(String orderId, boolean success, String reason) {
}
