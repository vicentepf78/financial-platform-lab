package com.financialplatform.sharedkernel.domain;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Cpf {

    private static final Pattern DIGITS_ONLY = Pattern.compile("\\d{11}");

    private final String value;

    private Cpf(String value) {
        this.value = Objects.requireNonNull(value, "CPF is required");
        if (!DIGITS_ONLY.matcher(value).matches()) {
            throw new IllegalArgumentException("CPF must contain exactly 11 digits");
        }
        if (!isValidChecksum(value)) {
            throw new IllegalArgumentException("Invalid CPF checksum");
        }
    }

    public static Cpf of(String raw) {
        String digits = raw.replaceAll("\\D", "");
        return new Cpf(digits);
    }

    public String value() {
        return value;
    }

    public String formatted() {
        return value.substring(0, 3) + "." +
                value.substring(3, 6) + "." +
                value.substring(6, 9) + "-" +
                value.substring(9, 11);
    }

    private static boolean isValidChecksum(String cpf) {
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }
        int firstDigit = calculateDigit(cpf, 9);
        int secondDigit = calculateDigit(cpf, 10);
        return cpf.charAt(9) == Character.forDigit(firstDigit, 10)
                && cpf.charAt(10) == Character.forDigit(secondDigit, 10);
    }

    private static int calculateDigit(String cpf, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (length + 1 - i);
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Cpf cpf = (Cpf) other;
        return value.equals(cpf.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return formatted();
    }
}
