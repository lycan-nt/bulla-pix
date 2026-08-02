package com.bullla.pix.domain;

public class PixTransactionNotFoundException extends RuntimeException {

    public PixTransactionNotFoundException(String transactionId) {
        super("Transação PIX não encontrada: " + transactionId);
    }
}
