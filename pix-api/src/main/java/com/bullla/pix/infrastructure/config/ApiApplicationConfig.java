package com.bullla.pix.infrastructure.config;

import com.bullla.pix.application.CreatePixTransactionUseCase;
import com.bullla.pix.application.GetPixTransactionUseCase;
import com.bullla.pix.application.port.IPixTransactionPublisher;
import com.bullla.pix.application.port.IPixTransactionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ApiApplicationConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    CreatePixTransactionUseCase createPixTransactionUseCase(
            IPixTransactionRepository repository,
            IPixTransactionPublisher publisher,
            Clock clock
    ) {
        return new CreatePixTransactionUseCase(repository, publisher, clock);
    }

    @Bean
    GetPixTransactionUseCase getPixTransactionUseCase(IPixTransactionRepository repository) {
        return new GetPixTransactionUseCase(repository);
    }
}
