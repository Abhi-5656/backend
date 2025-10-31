package com.wfm.experts.modules.wfm.employee.location.tracking.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class TrackingRabbitConfig {

    @Value("${tracking.rabbit.exchange:tracking.points}")
    private String exchangeName;

    @Value("${tracking.rabbit.queue.prefix:tracking.points.q}")
    private String queuePrefix;

    @Value("${tracking.rabbit.shards:8}")
    private int shards;

    @Bean
    public DirectExchange trackingExchange() {
        // direct works; consistent-hash needs plugin — we keep it simple
        return new DirectExchange(exchangeName, true, false);
        // (equivalent) return ExchangeBuilder.directExchange(exchangeName).durable(true).build();
    }

    @Bean
    public Declarables trackingBindings(DirectExchange trackingExchange) {
        List<Declarable> declarables = new ArrayList<>(shards * 2);

        for (int i = 1; i <= shards; i++) {
            String queueName = queuePrefix + i;

            Queue queue = QueueBuilder.durable(queueName)
                    .withArgument("x-queue-mode", "lazy") // disk-backed, low memory
                    .build();

            Binding binding = BindingBuilder.bind(queue)
                    .to(trackingExchange)
                    .with(queueName);

            declarables.add(queue);
            declarables.add(binding);
        }

        return new Declarables(declarables);
    }
}
