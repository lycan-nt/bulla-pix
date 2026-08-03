package com.bullla.pix.infrastructure.metrics;

import com.bullla.pix.application.port.IPixMetricsRecorder;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class MicrometerPixMetricsRecorder implements IPixMetricsRecorder {

    private static final String RESULT_METRIC = "pix.transactions.result";
    private static final String RETRY_METRIC = "pix.transactions.retry";
    private static final String PARTNER_LATENCY_METRIC = "pix.partner.call";

    private final MeterRegistry meterRegistry;

    public MicrometerPixMetricsRecorder(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void recordCompleted() {
        result("completed").increment();
    }

    @Override
    public void recordFailed() {
        result("failed").increment();
    }

    @Override
    public void recordRetryScheduled() {
        Counter.builder(RETRY_METRIC)
                .description("Tentativas de reprocessamento agendadas após falha temporária do parceiro")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordPartnerCall(Duration latency, boolean success) {
        Timer.builder(PARTNER_LATENCY_METRIC)
                .description("Latência das chamadas ao parceiro PIX (mock)")
                .tag("outcome", success ? "success" : "failure")
                .register(meterRegistry)
                .record(latency);
    }

    private Counter result(String status) {
        return Counter.builder(RESULT_METRIC)
                .description("Contagem de transações PIX por status final")
                .tag("status", status)
                .register(meterRegistry);
    }
}
