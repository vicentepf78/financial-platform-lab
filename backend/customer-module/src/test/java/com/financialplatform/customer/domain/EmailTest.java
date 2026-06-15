package com.financialplatform.customer.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Test
    void shouldCreateEmailWhenFormatIsValid() {
        Email email = Email.of("maria@example.com");

        assertThat(email.value()).isEqualTo("maria@example.com");
    }

    @Test
    void shouldTrimEmailWhenSurroundedByWhitespace() {
        Email email = Email.of("  maria@example.com  ");

        assertThat(email.value()).isEqualTo("maria@example.com");
    }

    @Test
    void shouldRejectEmailWhenBlank() {
        assertThatThrownBy(() -> Email.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void shouldRejectEmailWhenFormatIsInvalid() {
        assertThatThrownBy(() -> Email.of("not-an-email"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email");
    }
}
