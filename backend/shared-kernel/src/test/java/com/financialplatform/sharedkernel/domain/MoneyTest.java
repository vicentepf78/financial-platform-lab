package com.financialplatform.sharedkernel.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void shouldAddAmountsWhenSameCurrency() {
        Money total = Money.brl("10.00").add(Money.brl("5.50"));

        assertThat(total.amount()).isEqualByComparingTo("15.50");
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThatThrownBy(() -> Money.brl("-1.00"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
