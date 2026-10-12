package com.nailly.domain.shared;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void normalizesToTwoDecimalPlaces() {
        assertThat(Money.of("10").getAmount()).isEqualTo(new BigDecimal("10.00"));
        assertThat(Money.of("10.5")).isEqualTo(Money.of("10.50"));
    }

    @Test
    void addsAmounts() {
        assertThat(Money.of("10.50").plus(Money.of("4.75"))).isEqualTo(Money.of("15.25"));
        assertThat(Money.ZERO.plus(Money.of("3"))).isEqualTo(Money.of("3.00"));
    }

    @Test
    void rejectsNegativeAmount() {
        assertThatThrownBy(() -> Money.of("-0.01"))
                .isInstanceOf(DomainException.class)
                .hasMessage("O valor não pode ser negativo.");
    }

    @Test
    void rejectsMoreThanTwoDecimalPlaces() {
        assertThatThrownBy(() -> Money.of("10.005"))
                .isInstanceOf(DomainException.class)
                .hasMessage("O valor deve ter no máximo duas casas decimais.")
                .hasCauseInstanceOf(ArithmeticException.class);
    }
}
