package com.bullla.pix.application;

import com.bullla.pix.application.port.IPartnerPixClient;
import com.bullla.pix.application.port.IPixMetricsRecorder;
import com.bullla.pix.application.port.IPixTransactionRepository;
import com.bullla.pix.domain.PixStatus;
import com.bullla.pix.domain.PixTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessPixTransactionUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-06-12T15:00:00Z");

    @Mock
    private IPixTransactionRepository repository;

    @Mock
    private IPartnerPixClient partnerPixClient;

    @Mock
    private IPixMetricsRecorder metricsRecorder;

    private ProcessPixTransactionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProcessPixTransactionUseCase(
                repository,
                partnerPixClient,
                metricsRecorder,
                Clock.fixed(NOW, ZoneOffset.UTC),
                3
        );
    }

    @Test
    void shouldMarkCompleted_whenPartnerSucceeds() {
        PixTransaction tx = received("tx-1");
        when(repository.findByTransactionId("tx-1")).thenReturn(Optional.of(tx));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(partnerPixClient.sendPix(any(), any(), any(), any()))
                .thenReturn(IPartnerPixClient.PartnerResult.ok("ok"));

        useCase.execute("tx-1");

        assertThat(tx.getStatus()).isEqualTo(PixStatus.COMPLETED);
        assertThat(tx.getAttemptCount()).isEqualTo(1);
        verify(metricsRecorder).recordCompleted();
        verify(metricsRecorder).recordPartnerCall(any(Duration.class), eq(true));
    }

    @Test
    void shouldThrowTemporaryFailure_whenPartnerFailsAndAttemptsRemain() {
        PixTransaction tx = received("tx-1");
        when(repository.findByTransactionId("tx-1")).thenReturn(Optional.of(tx));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(partnerPixClient.sendPix(any(), any(), any(), any()))
                .thenReturn(IPartnerPixClient.PartnerResult.failure("indisponível"));

        assertThatThrownBy(() -> useCase.execute("tx-1"))
                .isInstanceOf(PartnerTemporaryFailureException.class);

        assertThat(tx.getStatus()).isEqualTo(PixStatus.PROCESSING);
        assertThat(tx.getAttemptCount()).isEqualTo(1);
        verify(metricsRecorder).recordRetryScheduled();
        verify(metricsRecorder).recordPartnerCall(any(Duration.class), eq(false));
    }

    @Test
    void shouldMarkFailed_whenMaxAttemptsReached() {
        PixTransaction tx = received("tx-1");
        tx.registerAttempt();
        tx.registerAttempt();
        when(repository.findByTransactionId("tx-1")).thenReturn(Optional.of(tx));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(partnerPixClient.sendPix(any(), any(), any(), any()))
                .thenReturn(IPartnerPixClient.PartnerResult.failure("indisponível"));

        useCase.execute("tx-1");

        assertThat(tx.getStatus()).isEqualTo(PixStatus.FAILED);
        assertThat(tx.getAttemptCount()).isEqualTo(3);
        assertThat(tx.getFailureReason()).contains("indisponível");
        verify(metricsRecorder).recordFailed();
    }

    @Test
    void shouldSkip_whenAlreadyCompleted() {
        PixTransaction tx = received("tx-1");
        tx.markCompleted(NOW);
        when(repository.findByTransactionId("tx-1")).thenReturn(Optional.of(tx));

        useCase.execute("tx-1");

        verify(partnerPixClient, never()).sendPix(any(), any(), any(), any());
        verifyNoInteractions(metricsRecorder);
    }

    private PixTransaction received(String id) {
        return PixTransaction.create(id, new BigDecimal("10.00"), "key", "desc", NOW);
    }
}
