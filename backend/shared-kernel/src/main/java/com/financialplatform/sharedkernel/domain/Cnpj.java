package com.financialplatform.sharedkernel.domain;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Cnpj {

    private static final Pattern DIGITS_ONLY = Pattern.compile("\\d{14}");
    private static final int[] WEIGHTS_FIRST = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] WEIGHTS_SECOND = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private final String value;

    private Cnpj(String value) {
        this.value = Objects.requireNonNull(value, "CNPJ is required");
        if (!DIGITS_ONLY.matcher(value).matches()) {
            throw new IllegalArgumentException("CNPJ must contain exactly 14 digits");
        }
        if (!isValidChecksum(value)) {
            throw new IllegalArgumentException("Invalid CNPJ checksum");
        }
    }

    public static Cnpj of(String raw) {
        String digits = raw.replaceAll("\\D", "");
        return new Cnpj(digits);
    }

    public String value() {
        return value;
    }

    public String formatted() {
        return value.substring(0, 2) + "." +
                value.substring(2, 5) + "." +
                value.substring(5, 8) + "/" +
                value.substring(8, 12) + "-" +
                value.substring(12, 14);
    }

    private static boolean isValidChecksum(String cnpj) {
        if (cnpj.chars().distinct().count() == 1) {
            return false;
        }
        int firstDigit = calculateDigit(cnpj, WEIGHTS_FIRST);
        int secondDigit = calculateDigit(cnpj + firstDigit, WEIGHTS_SECOND);
        return cnpj.charAt(12) == Character.forDigit(firstDigit, 10)
                && cnpj.charAt(13) == Character.forDigit(secondDigit, 10);
    }

    private static int calculateDigit(String value, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += Character.getNumericValue(value.charAt(i)) * weights[i];
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
        Cnpj cnpj = (Cnpj) other;
        return value.equals(cnpj.value);
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
