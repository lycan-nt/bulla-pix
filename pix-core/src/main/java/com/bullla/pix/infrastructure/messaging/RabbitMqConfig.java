package com.bullla.pix.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    DirectExchange pixExchange() {
        return new DirectExchange(PixMessagingTopology.EXCHANGE, true, false);
    }

    @Bean
    DirectExchange pixDeadLetterExchange() {
        return new DirectExchange(PixMessagingTopology.DLX, true, false);
    }

    @Bean
    Queue pixQueue() {
        return QueueBuilder.durable(PixMessagingTopology.QUEUE)
                .withArgument("x-dead-letter-exchange", PixMessagingTopology.DLX)
                .withArgument("x-dead-letter-routing-key", PixMessagingTopology.DLQ)
                .build();
    }

    @Bean
    Queue pixDlq() {
        return QueueBuilder.durable(PixMessagingTopology.DLQ).build();
    }

    @Bean
    Binding pixBinding(Queue pixQueue, DirectExchange pixExchange) {
        return BindingBuilder.bind(pixQueue).to(pixExchange).with(PixMessagingTopology.ROUTING_KEY);
    }

    @Bean
    Binding pixDlqBinding(Queue pixDlq, DirectExchange pixDeadLetterExchange) {
        return BindingBuilder.bind(pixDlq).to(pixDeadLetterExchange).with(PixMessagingTopology.DLQ);
    }
}
