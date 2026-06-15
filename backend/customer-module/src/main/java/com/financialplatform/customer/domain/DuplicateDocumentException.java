package com.financialplatform.customer.domain;

public class DuplicateDocumentException extends RuntimeException {

    public DuplicateDocumentException(String documentDigits) {
        super("Customer already exists with document: " + documentDigits);
    }
}
