package com.bullla.pix.infrastructure.messaging;

import com.bullla.pix.application.port.IPixTransactionPublisher;
import com.bullla.pix.infrastructure.logging.CorrelationIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PixTransactionPublisher implements IPixTransactionPublisher {

    private static final Logger log = LoggerFactory.getLogger(PixTransactionPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public PixTransactionPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void enqueue(String transactionId) {
        String correlationId = MDC.get(CorrelationIdContext.MDC_KEY);
        rabbitTemplate.convertAndSend(
                PixMessagingTopology.EXCHANGE,
                PixMessagingTopology.ROUTING_KEY,
                transactionId,
                message -> {
                    if (correlationId != null) {
                        message.getMessageProperties()
                                .setHeader(PixMessagingTopology.CORRELATION_ID_HEADER, correlationId);
                    }
                    return message;
                }
        );
        log.info("PIX {} enfileirado para processamento", transactionId);
    }
}
