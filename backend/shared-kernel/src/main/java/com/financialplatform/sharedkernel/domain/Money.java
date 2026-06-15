package com.financialplatform.sharedkernel.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public final class Money {

    private static final int SCALE = 2;
    private static final Currency BRL = Currency.getInstance("BRL");

    private final BigDecimal amount;
    private final Currency currency;

    private Money(BigDecimal amount, Currency currency) {
        this.amount = Objects.requireNonNull(amount, "Amount is required").setScale(SCALE, RoundingMode.HALF_UP);
        this.currency = Objects.requireNonNull(currency, "Currency is required");
        if (this.amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be zero or positive");
        }
    }

    public static Money brl(String amount) {
        return new Money(new BigDecimal(amount), BRL);
    }

    public static Money brl(BigDecimal amount) {
        return new Money(amount, BRL);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO.setScale(SCALE), BRL);
    }

    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        assertSameCurrency(other);
        BigDecimal result = amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Resulting amount cannot be negative");
        }
        return new Money(result, currency);
    }

    public boolean isGreaterThanOrEqual(Money other) {
        assertSameCurrency(other);
        return amount.compareTo(other.amount) >= 0;
    }

    public BigDecimal amount() {
        return amount;
    }

    public Currency currency() {
        return currency;
    }

    private void assertSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Money money = (Money) other;
        return amount.compareTo(money.amount) == 0 && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return currency.getCurrencyCode() + " " + amount;
    }
}
