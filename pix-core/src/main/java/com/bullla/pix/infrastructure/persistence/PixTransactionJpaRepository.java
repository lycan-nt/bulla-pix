package com.bullla.pix.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PixTransactionJpaRepository extends JpaRepository<PixTransactionEntity, String> {
}
