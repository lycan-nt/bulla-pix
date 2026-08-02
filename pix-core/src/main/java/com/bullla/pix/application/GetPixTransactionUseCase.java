package com.bullla.pix.application;

import com.bullla.pix.application.port.IPixTransactionRepository;
import com.bullla.pix.domain.PixTransaction;
import com.bullla.pix.domain.PixTransactionNotFoundException;

public class GetPixTransactionUseCase {

    private final IPixTransactionRepository repository;

    public GetPixTransactionUseCase(IPixTransactionRepository repository) {
        this.repository = repository;
    }

    public PixTransaction execute(String transactionId) {
        return repository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PixTransactionNotFoundException(transactionId));
    }
}
