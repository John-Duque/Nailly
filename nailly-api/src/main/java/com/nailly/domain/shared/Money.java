package com.nailly.domain.shared;

import java.math.BigDecimal;
import java.util.Objects;

/** Valor em reais, nunca negativo, sempre com duas casas decimais (DECIMAL(10,2) no banco). */
public final class Money {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    private final BigDecimal amount;

    public Money(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount");
        if (amount.signum() < 0) {
            throw new DomainException("O valor não pode ser negativo.");
        }
        this.amount = DomainValidation.requireScale(amount, 2, "O valor deve ter no máximo duas casas decimais.");
    }

    public static Money of(String amount) {
        return new Money(new BigDecimal(amount));
    }

    public Money plus(Money other) {
        return new Money(amount.add(other.amount));
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof Money money && amount.equals(money.amount);
    }

    @Override
    public int hashCode() {
        return amount.hashCode();
    }

    @Override
    public String toString() {
        return "R$ " + amount;
    }
}
