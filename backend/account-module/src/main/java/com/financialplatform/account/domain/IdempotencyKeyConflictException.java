package com.financialplatform.account.domain;

public class IdempotencyKeyConflictException extends RuntimeException {

    public IdempotencyKeyConflictException(String idempotencyKey) {
        super("Idempotency key already used with a different transfer request: " + idempotencyKey);
    }
}
