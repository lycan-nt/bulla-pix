package com.bullla.pix.application;

public class PartnerTemporaryFailureException extends RuntimeException {

    public PartnerTemporaryFailureException(String transactionId, String message) {
        super("Falha temporária do parceiro para " + transactionId + ": " + message);
    }
}
