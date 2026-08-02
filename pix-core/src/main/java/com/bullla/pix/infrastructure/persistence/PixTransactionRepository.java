package com.bullla.pix.infrastructure.persistence;

import com.bullla.pix.application.port.IPixTransactionRepository;
import com.bullla.pix.domain.PixTransaction;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class PixTransactionRepository implements IPixTransactionRepository {

    private final PixTransactionJpaRepository pixTransactionJpaRepository;

    public PixTransactionRepository(PixTransactionJpaRepository pixTransactionJpaRepository) {
        this.pixTransactionJpaRepository = pixTransactionJpaRepository;
    }

    @Override
    @Transactional
    public PixTransaction save(PixTransaction transaction) {
        PixTransactionEntity entity = pixTransactionJpaRepository.findById(transaction.getTransactionId())
                .orElseGet(PixTransactionEntity::new);

        if (entity.getTransactionId() == null) {
            entity = PixTransactionMapper.toEntity(transaction);
        } else {
            PixTransactionMapper.copyToEntity(transaction, entity);
        }

        return PixTransactionMapper.toDomain(pixTransactionJpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PixTransaction> findByTransactionId(String transactionId) {
        return pixTransactionJpaRepository.findById(transactionId).map(PixTransactionMapper::toDomain);
    }
}
