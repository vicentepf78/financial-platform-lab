package com.financialplatform.customer.domain;

public class NoFieldsToUpdateException extends RuntimeException {

    public NoFieldsToUpdateException() {
        super("No fields to update");
    }
}
