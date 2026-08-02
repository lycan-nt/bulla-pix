package com.bullla.pix.application.port;

import com.bullla.pix.domain.PixTransaction;

import java.util.Optional;

public interface IPixTransactionRepository {

    PixTransaction save(PixTransaction transaction);

    Optional<PixTransaction> findByTransactionId(String transactionId);
}
