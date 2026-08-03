package com.bullla.pix.infrastructure.messaging;

public final class PixMessagingTopology {

    public static final String EXCHANGE = "pix.exchange";
    public static final String QUEUE = "pix.transactions";
    public static final String ROUTING_KEY = "pix.transaction";
    public static final String DLQ = "pix.transactions.dlq";
    public static final String DLX = "pix.exchange.dlx";
    public static final String CORRELATION_ID_HEADER = "correlationId";

    private PixMessagingTopology() {
    }
}
