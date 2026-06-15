package com.financialplatform.sharedkernel.domain;

import java.util.Objects;
import java.util.UUID;

public final class Identifier {

    private final UUID value;

    private Identifier(UUID value) {
        this.value = Objects.requireNonNull(value, "Identifier value is required");
    }

    public static Identifier generate() {
        return new Identifier(UUID.randomUUID());
    }

    public static Identifier of(UUID value) {
        return new Identifier(value);
    }

    public static Identifier of(String value) {
        Objects.requireNonNull(value, "Identifier string is required");
        return new Identifier(UUID.fromString(value));
    }

    public UUID value() {
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
        Identifier that = (Identifier) other;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
