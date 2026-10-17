package io.github.fathurrh.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PaymentProcessorTest {

    private final PaymentProcessor processor = new PaymentProcessor();

    @Test
    void chargesOrdersWithinTheLimit() {
        var result = processor.charge(new OrderCreatedEvent("o-1", "c-1", new BigDecimal("250")));
        assertThat(result.success()).isTrue();
    }

    @Test
    void declinesOrdersOverTheLimit() {
        var result = processor.charge(new OrderCreatedEvent("o-2", "c-1", new BigDecimal("1000.01")));
        assertThat(result.success()).isFalse();
        assertThat(result.reason()).isEqualTo("amount over limit");
    }

    @Test
    void declinesNonPositiveAmounts() {
        var result = processor.charge(new OrderCreatedEvent("o-3", "c-1", BigDecimal.ZERO));
        assertThat(result.success()).isFalse();
    }
}
