package com.bullla.pix.application;

import com.bullla.pix.application.port.IPartnerPixClient;
import com.bullla.pix.application.port.IPixMetricsRecorder;
import com.bullla.pix.application.port.IPixTransactionRepository;
import com.bullla.pix.domain.PixStatus;
import com.bullla.pix.domain.PixTransaction;
import com.bullla.pix.domain.PixTransactionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class ProcessPixTransactionUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessPixTransactionUseCase.class);

    private final IPixTransactionRepository repository;
    private final IPartnerPixClient partnerPixClient;
    private final IPixMetricsRecorder metricsRecorder;
    private final Clock clock;
    private final int maxAttempts;

    public ProcessPixTransactionUseCase(
            IPixTransactionRepository repository,
            IPartnerPixClient partnerPixClient,
            IPixMetricsRecorder metricsRecorder,
            Clock clock,
            int maxAttempts
    ) {
        this.repository = repository;
        this.partnerPixClient = partnerPixClient;
        this.metricsRecorder = metricsRecorder;
        this.clock = clock;
        this.maxAttempts = maxAttempts;
    }

    public void execute(String transactionId) {
        PixTransaction transaction = loadTransaction(transactionId);

        if (isTerminal(transaction)) {
            log.info("Ignorando transação em status terminal {}", transactionId);
            return;
        }

        beginAttempt(transaction);
        IPartnerPixClient.PartnerResult result = callPartner(transaction);
        handlePartnerResult(transaction, result);
    }

    private PixTransaction loadTransaction(String transactionId) {
        return repository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PixTransactionNotFoundException(transactionId));
    }

    private boolean isTerminal(PixTransaction transaction) {
        return transaction.getStatus() == PixStatus.COMPLETED
                || transaction.getStatus() == PixStatus.FAILED;
    }

    private void beginAttempt(PixTransaction transaction) {
        transaction.markProcessing(Instant.now(clock));
        transaction.registerAttempt();
        repository.save(transaction);
    }

    private IPartnerPixClient.PartnerResult callPartner(PixTransaction transaction) {
        long startNanos = System.nanoTime();
        IPartnerPixClient.PartnerResult result = partnerPixClient.sendPix(
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getPixKey(),
                transaction.getDescription()
        );
        Duration latency = Duration.ofNanos(System.nanoTime() - startNanos);
        metricsRecorder.recordPartnerCall(latency, result.success());
        return result;
    }

    private void handlePartnerResult(PixTransaction transaction, IPartnerPixClient.PartnerResult result) {
        if (result.success()) {
            complete(transaction);
            return;
        }

        if (transaction.getAttemptCount() >= maxAttempts) {
            failPermanently(transaction, result.message());
            return;
        }

        scheduleRetry(transaction, result.message());
    }

    private void complete(PixTransaction transaction) {
        transaction.markCompleted(Instant.now(clock));
        repository.save(transaction);
        metricsRecorder.recordCompleted();
        log.info("PIX {} concluído", transaction.getTransactionId());
    }

    private void failPermanently(PixTransaction transaction, String reason) {
        transaction.markFailed(reason, Instant.now(clock));
        repository.save(transaction);
        metricsRecorder.recordFailed();
        log.warn(
                "PIX {} falhou após {} tentativas: {}",
                transaction.getTransactionId(),
                transaction.getAttemptCount(),
                reason
        );
    }

    private void scheduleRetry(PixTransaction transaction, String reason) {
        repository.save(transaction);
        metricsRecorder.recordRetryScheduled();
        throw new PartnerTemporaryFailureException(transaction.getTransactionId(), reason);
    }
}
