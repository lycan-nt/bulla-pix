package com.bullla.pix.application;

import com.bullla.pix.application.port.IPixTransactionRepository;
import com.bullla.pix.domain.PixTransaction;
import com.bullla.pix.domain.PixTransactionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPixTransactionUseCaseTest {

    @Mock
    private IPixTransactionRepository repository;

    private GetPixTransactionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetPixTransactionUseCase(repository);
    }

    @Test
    void shouldReturnTransaction_whenFound() {
        PixTransaction tx = PixTransaction.create(
                "tx-123456",
                new BigDecimal("150.75"),
                "cliente@email.com",
                "Pagamento de fatura",
                Instant.parse("2026-06-12T15:00:00Z")
        );
        when(repository.findByTransactionId("tx-123456")).thenReturn(Optional.of(tx));

        assertThat(useCase.execute("tx-123456").getTransactionId()).isEqualTo("tx-123456");
    }

    @Test
    void shouldThrow_whenNotFound() {
        when(repository.findByTransactionId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("missing"))
                .isInstanceOf(PixTransactionNotFoundException.class);
    }
}
