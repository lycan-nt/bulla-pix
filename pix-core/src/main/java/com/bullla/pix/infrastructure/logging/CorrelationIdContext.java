package com.bullla.pix.infrastructure.logging;

public final class CorrelationIdContext {

    public static final String MDC_KEY = "correlationId";
    public static final String TRANSACTION_ID_MDC_KEY = "transactionId";

    private CorrelationIdContext() {
    }
}
