package com.bullla.pix.application;

import com.bullla.pix.application.port.IPixTransactionPublisher;
import com.bullla.pix.application.port.IPixTransactionRepository;
import com.bullla.pix.domain.IdempotencyConflictException;
import com.bullla.pix.domain.PixTransaction;

import java.time.Clock;
import java.time.Instant;

public class CreatePixTransactionUseCase {

    private final IPixTransactionRepository repository;
    private final IPixTransactionPublisher publisher;
    private final Clock clock;

    public CreatePixTransactionUseCase(
            IPixTransactionRepository repository,
            IPixTransactionPublisher publisher,
            Clock clock
    ) {
        this.repository = repository;
        this.publisher = publisher;
        this.clock = clock;
    }

    public PixTransaction execute(CreatePixCommand command) {
        return repository.findByTransactionId(command.transactionId())
                .map(existing -> handleExisting(existing, command))
                .orElseGet(() -> createNew(command));
    }

    private PixTransaction handleExisting(PixTransaction existing, CreatePixCommand command) {
        if (!existing.matchesPayload(command.amount(), command.pixKey(), command.description())) {
            throw new IdempotencyConflictException(command.transactionId());
        }
        return existing;
    }

    private PixTransaction createNew(CreatePixCommand command) {
        Instant now = Instant.now(clock);
        PixTransaction created = PixTransaction.create(
                command.transactionId(),
                command.amount(),
                command.pixKey(),
                command.description(),
                now
        );
        PixTransaction saved = repository.save(created);
        publisher.enqueue(saved.getTransactionId());
        return saved;
    }
}
