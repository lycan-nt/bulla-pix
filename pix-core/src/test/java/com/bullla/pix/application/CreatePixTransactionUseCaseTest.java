package com.bullla.pix.application;

import com.bullla.pix.application.port.IPixTransactionPublisher;
import com.bullla.pix.application.port.IPixTransactionRepository;
import com.bullla.pix.domain.IdempotencyConflictException;
import com.bullla.pix.domain.PixStatus;
import com.bullla.pix.domain.PixTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePixTransactionUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-06-12T15:00:00Z");

    @Mock
    private IPixTransactionRepository repository;

    @Mock
    private IPixTransactionPublisher publisher;

    private CreatePixTransactionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreatePixTransactionUseCase(repository, publisher, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void shouldCreateTransactionAndEnqueue_whenNewTransactionId() {
        when(repository.findByTransactionId("tx-123456")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PixTransaction result = useCase.execute(command("tx-123456", "150.75", "cliente@email.com", "Pagamento de fatura"));

        assertThat(result.getStatus()).isEqualTo(PixStatus.RECEIVED);
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
        verify(publisher).enqueue("tx-123456");
    }

    @Test
    void shouldReturnExisting_whenSameTransactionIdAndPayload() {
        PixTransaction existing = PixTransaction.create(
                "tx-123456",
                new BigDecimal("150.75"),
                "cliente@email.com",
                "Pagamento de fatura",
                NOW
        );
        when(repository.findByTransactionId("tx-123456")).thenReturn(Optional.of(existing));

        PixTransaction result = useCase.execute(command("tx-123456", "150.75", "cliente@email.com", "Pagamento de fatura"));

        assertThat(result).isSameAs(existing);
        verify(repository, never()).save(any());
        verify(publisher, never()).enqueue(any());
    }

    @Test
    void shouldThrowConflict_whenSameTransactionIdWithDifferentPayload() {
        PixTransaction existing = PixTransaction.create(
                "tx-123456",
                new BigDecimal("150.75"),
                "cliente@email.com",
                "Pagamento de fatura",
                NOW
        );
        when(repository.findByTransactionId("tx-123456")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(command("tx-123456", "200.00", "cliente@email.com", "Outro")))
                .isInstanceOf(IdempotencyConflictException.class);

        verify(publisher, never()).enqueue(any());
    }

    @Test
    void shouldPersistBeforeEnqueue() {
        when(repository.findByTransactionId("tx-1")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(command("tx-1", "10.00", "key", "desc"));

        ArgumentCaptor<PixTransaction> captor = ArgumentCaptor.forClass(PixTransaction.class);
        verify(repository).save(captor.capture());
        verify(publisher).enqueue("tx-1");
        assertThat(captor.getValue().getStatus()).isEqualTo(PixStatus.RECEIVED);
    }

    private CreatePixCommand command(String id, String amount, String key, String description) {
        return new CreatePixCommand(id, new BigDecimal(amount), key, description);
    }
}
