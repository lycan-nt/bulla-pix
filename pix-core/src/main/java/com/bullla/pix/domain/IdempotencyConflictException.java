package com.bullla.pix.domain;

public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException(String transactionId) {
        super("transactionId já existe com um payload diferente: " + transactionId);
    }
}
