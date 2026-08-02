package com.bullla.pix.infrastructure.persistence;

import com.bullla.pix.domain.PixTransaction;

final class PixTransactionMapper {

    private PixTransactionMapper() {
    }

    static PixTransactionEntity toEntity(PixTransaction domain) {
        PixTransactionEntity entity = new PixTransactionEntity();
        entity.setTransactionId(domain.getTransactionId());
        entity.setAmount(domain.getAmount());
        entity.setPixKey(domain.getPixKey());
        entity.setDescription(domain.getDescription());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setFailureReason(domain.getFailureReason());
        entity.setAttemptCount(domain.getAttemptCount());
        return entity;
    }

    static void copyToEntity(PixTransaction domain, PixTransactionEntity entity) {
        entity.setAmount(domain.getAmount());
        entity.setPixKey(domain.getPixKey());
        entity.setDescription(domain.getDescription());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setFailureReason(domain.getFailureReason());
        entity.setAttemptCount(domain.getAttemptCount());
    }

    static PixTransaction toDomain(PixTransactionEntity entity) {
        return new PixTransaction(
                entity.getTransactionId(),
                entity.getAmount(),
                entity.getPixKey(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getFailureReason(),
                entity.getAttemptCount()
        );
    }
}
