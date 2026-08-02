package com.bullla.pix.infrastructure.config;

import com.bullla.pix.application.ProcessPixTransactionUseCase;
import com.bullla.pix.application.port.IPartnerPixClient;
import com.bullla.pix.application.port.IPixMetricsRecorder;
import com.bullla.pix.application.port.IPixTransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class WorkerApplicationConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    ProcessPixTransactionUseCase processPixTransactionUseCase(
            IPixTransactionRepository repository,
            IPartnerPixClient partnerPixClient,
            IPixMetricsRecorder metricsRecorder,
            Clock clock,
            @Value("${app.partner.max-attempts:3}") int maxAttempts
    ) {
        return new ProcessPixTransactionUseCase(repository, partnerPixClient, metricsRecorder, clock, maxAttempts);
    }
}
