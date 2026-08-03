package com.bullla.pix.infrastructure.partner;

import com.bullla.pix.application.port.IPartnerPixClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class MockPartnerPixClient implements IPartnerPixClient {

    private static final Logger log = LoggerFactory.getLogger(MockPartnerPixClient.class);

    private final long latencyMs;
    private final double failureRate;
    private final ObservationRegistry observationRegistry;

    public MockPartnerPixClient(
            @Value("${app.partner.latency-ms:2000}") long latencyMs,
            @Value("${app.partner.failure-rate:0.2}") double failureRate,
            ObservationRegistry observationRegistry
    ) {
        this.latencyMs = latencyMs;
        this.failureRate = failureRate;
        this.observationRegistry = observationRegistry;
    }

    @Override
    @CircuitBreaker(name = "partnerPix", fallbackMethod = "fallback")
    public PartnerResult sendPix(String transactionId, BigDecimal amount, String pixKey, String description) {
        return Observation.createNotStarted("pix.partner.invoke", observationRegistry)
                .lowCardinalityKeyValue("partner", "mock")
                .observe(() -> doSendPix(transactionId));
    }

    private PartnerResult doSendPix(String transactionId) {
        log.info("Chamando parceiro para o PIX {} (latência ~{}ms)", transactionId, latencyMs);
        sleep();

        if (ThreadLocalRandom.current().nextDouble() < failureRate) {
            log.warn("Falha temporária simulada do parceiro para {}", transactionId);
            return PartnerResult.failure("Parceiro temporariamente indisponível");
        }

        return PartnerResult.ok("Aceito pelo parceiro");
    }

    @SuppressWarnings("unused")
    private PartnerResult fallback(
            String transactionId,
            BigDecimal amount,
            String pixKey,
            String description,
            Throwable throwable
    ) {
        log.error("Fallback do circuit breaker para {}: {}", transactionId, throwable.toString());
        return PartnerResult.failure("Circuit breaker aberto ou erro no parceiro: " + throwable.getMessage());
    }

    private void sleep() {
        try {
            Thread.sleep(latencyMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrompido ao aguardar resposta do parceiro", e);
        }
    }
}
