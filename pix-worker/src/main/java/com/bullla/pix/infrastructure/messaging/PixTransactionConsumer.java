package com.bullla.pix.infrastructure.messaging;

import com.bullla.pix.application.PartnerTemporaryFailureException;
import com.bullla.pix.application.ProcessPixTransactionUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PixTransactionConsumer {

    private static final Logger log = LoggerFactory.getLogger(PixTransactionConsumer.class);

    private final ProcessPixTransactionUseCase processPixTransactionUseCase;

    public PixTransactionConsumer(ProcessPixTransactionUseCase processPixTransactionUseCase) {
        this.processPixTransactionUseCase = processPixTransactionUseCase;
    }

    @RabbitListener(queues = PixMessagingTopology.QUEUE)
    public void onMessage(String transactionId) {
        log.info("Consumindo transação PIX {}", transactionId);
        try {
            processPixTransactionUseCase.execute(transactionId);
        } catch (PartnerTemporaryFailureException ex) {
            log.warn("Falha temporária ao processar {}: {}", transactionId, ex.getMessage());
            throw ex;
        }
    }
}
