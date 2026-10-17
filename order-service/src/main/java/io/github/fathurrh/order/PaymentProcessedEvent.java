package io.github.fathurrh.order;

/** Consumed from the "payments" topic. Mirrors the event payment-service publishes. */
public record PaymentProcessedEvent(String orderId, boolean success, String reason) {
}
