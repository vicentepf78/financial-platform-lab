package com.financialplatform.customer.domain;

public class CustomerTypeMismatchException extends RuntimeException {

    public CustomerTypeMismatchException(CustomerType type, String documentDigits) {
        super(buildMessage(type, documentDigits));
    }

    private static String buildMessage(CustomerType type, String documentDigits) {
        String expected = type == CustomerType.INDIVIDUAL ? "CPF" : "CNPJ";
        return "Expected " + expected + " for customer type " + type + " but received " + documentDigits.length() + " digits";
    }
}
