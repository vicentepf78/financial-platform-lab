package com.financialplatform.customer.domain;

public sealed interface Document permits CpfDocument, CnpjDocument {

    String digits();

    String formatted();

    static Document of(CustomerType type, String raw) {
        String documentDigits = raw.replaceAll("\\D", "");
        return switch (type) {
            case INDIVIDUAL -> createCpfDocument(type, documentDigits);
            case COMPANY -> createCnpjDocument(type, documentDigits);
        };
    }

    private static Document createCpfDocument(CustomerType type, String documentDigits) {
        if (documentDigits.length() != 11) {
            throw new CustomerTypeMismatchException(type, documentDigits);
        }
        return new CpfDocument(documentDigits);
    }

    private static Document createCnpjDocument(CustomerType type, String documentDigits) {
        if (documentDigits.length() != 14) {
            throw new CustomerTypeMismatchException(type, documentDigits);
        }
        return new CnpjDocument(documentDigits);
    }
}
