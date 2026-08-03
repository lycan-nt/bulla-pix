package com.bullla.pix.infrastructure.messaging;

import com.bullla.pix.application.PartnerTemporaryFailureException;
import com.bullla.pix.application.ProcessPixTransactionUseCase;
import com.bullla.pix.infrastructure.logging.CorrelationIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class PixTransactionConsumer {

    private static final Logger log = LoggerFactory.getLogger(PixTransactionConsumer.class);

    private final ProcessPixTransactionUseCase processPixTransactionUseCase;

    public PixTransactionConsumer(ProcessPixTransactionUseCase processPixTransactionUseCase) {
        this.processPixTransactionUseCase = processPixTransactionUseCase;
    }

    @RabbitListener(queues = PixMessagingTopology.QUEUE)
    public void onMessage(
            String transactionId,
            @Header(value = PixMessagingTopology.CORRELATION_ID_HEADER, required = false) String correlationId
    ) {
        MDC.put(CorrelationIdContext.TRANSACTION_ID_MDC_KEY, transactionId);
        if (correlationId != null) {
            MDC.put(CorrelationIdContext.MDC_KEY, correlationId);
        }
        try {
            log.info("Consumindo transação PIX {}", transactionId);
            processPixTransactionUseCase.execute(transactionId);
        } catch (PartnerTemporaryFailureException ex) {
            log.warn("Falha temporária ao processar {}: {}", transactionId, ex.getMessage());
            throw ex;
        } finally {
            MDC.remove(CorrelationIdContext.TRANSACTION_ID_MDC_KEY);
            MDC.remove(CorrelationIdContext.MDC_KEY);
        }
    }
}
