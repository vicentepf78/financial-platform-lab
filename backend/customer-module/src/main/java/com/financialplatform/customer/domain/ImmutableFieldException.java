package com.financialplatform.customer.domain;

public class ImmutableFieldException extends RuntimeException {

    public ImmutableFieldException(String fieldName) {
        super("Field cannot be changed: " + fieldName);
    }
}
