package com.bullla.pix.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class PixTransaction {

    private final String transactionId;
    private final BigDecimal amount;
    private final String pixKey;
    private final String description;
    private PixStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private String failureReason;
    private int attemptCount;

    public PixTransaction(
            String transactionId,
            BigDecimal amount,
            String pixKey,
            String description,
            PixStatus status,
            Instant createdAt,
            Instant updatedAt,
            String failureReason,
            int attemptCount
    ) {
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId");
        this.amount = Objects.requireNonNull(amount, "amount");
        this.pixKey = Objects.requireNonNull(pixKey, "pixKey");
        this.description = description;
        this.status = Objects.requireNonNull(status, "status");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
        this.failureReason = failureReason;
        this.attemptCount = attemptCount;
    }

    public static PixTransaction create(
            String transactionId,
            BigDecimal amount,
            String pixKey,
            String description,
            Instant now
    ) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount deve ser positivo");
        }
        return new PixTransaction(
                transactionId,
                amount,
                pixKey,
                description,
                PixStatus.RECEIVED,
                now,
                now,
                null,
                0
        );
    }

    public boolean matchesPayload(BigDecimal amount, String pixKey, String description) {
        return this.amount.compareTo(amount) == 0
                && this.pixKey.equals(pixKey)
                && Objects.equals(this.description, description);
    }

    public void markProcessing(Instant now) {
        this.status = PixStatus.PROCESSING;
        this.updatedAt = now;
    }

    public void markCompleted(Instant now) {
        this.status = PixStatus.COMPLETED;
        this.updatedAt = now;
        this.failureReason = null;
    }

    public void markFailed(String reason, Instant now) {
        this.status = PixStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = now;
    }

    public void registerAttempt() {
        this.attemptCount++;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPixKey() {
        return pixKey;
    }

    public String getDescription() {
        return description;
    }

    public PixStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public int getAttemptCount() {
        return attemptCount;
    }
}
