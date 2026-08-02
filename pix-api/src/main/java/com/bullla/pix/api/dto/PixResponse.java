package com.bullla.pix.api.dto;

import com.bullla.pix.domain.PixStatus;
import com.bullla.pix.domain.PixTransaction;

import java.math.BigDecimal;
import java.time.Instant;

public record PixResponse(
        String transactionId,
        BigDecimal amount,
        String pixKey,
        String description,
        PixStatus status,
        Instant createdAt,
        Instant updatedAt,
        String failureReason
) {
    public static PixResponse from(PixTransaction tx) {
        return new PixResponse(
                tx.getTransactionId(),
                tx.getAmount(),
                tx.getPixKey(),
                tx.getDescription(),
                tx.getStatus(),
                tx.getCreatedAt(),
                tx.getUpdatedAt(),
                tx.getFailureReason()
        );
    }
}
