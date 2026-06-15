package com.financialplatform.customer.domain;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Email {

    private static final Pattern EMAIL_FORMAT = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final String value;

    private Email(String value) {
        this.value = value;
    }

    public static Email of(String raw) {
        Objects.requireNonNull(raw, "Email is required");
        if (raw.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        String normalized = raw.trim();
        if (!EMAIL_FORMAT.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        return new Email(normalized);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Email email = (Email) other;
        return value.equals(email.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
