package com.wfm.experts.notificationengine.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class RabbitMQConfig {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConfig.class);

    // --- Exchange Names ---
    @Value("${rabbitmq.exchange.notification}")
    private String notificationExchangeName;

    @Value("${rabbitmq.exchange.deadLetter}")
    private String deadLetterExchangeName;

    // --- Queue Names ---
    @Value("${rabbitmq.queue.email}")
    private String emailQueueName;

    @Value("${rabbitmq.queue.push}")
    private String pushQueueName;

    @Value("${rabbitmq.queue.dlq.email}")
    private String emailDlqName;

    @Value("${rabbitmq.queue.dlq.push}")
    private String pushDlqName;

    // --- Routing Keys ---
    @Value("${rabbitmq.routingkey.email}")
    private String emailRoutingKey; // e.g., notification.email.#

    @Value("${rabbitmq.routingkey.push}")
    private String pushRoutingKey;  // e.g., notification.push.#

    // Routing keys for DLQs
    @Value("${rabbitmq.routingkey.dlq.email}")
    private String emailDlqRoutingKey; // e.g., dlq.email

    @Value("${rabbitmq.routingkey.dlq.push}")
    private String pushDlqRoutingKey;  // e.g., dlq.push


    // === Exchanges ===
    @Bean
    public TopicExchange notificationExchange() {
        logger.info("Creating TopicExchange: {}", notificationExchangeName);
        return new TopicExchange(notificationExchangeName, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        logger.info("Creating DirectExchange (DLX): {}", deadLetterExchangeName);
        return new DirectExchange(deadLetterExchangeName, true, false);
    }

    // === Queues ===
    @Bean
    public Queue emailQueue() {
        logger.info("Creating Queue: {} with DLX: {} and DLQ routing key: {}", emailQueueName, deadLetterExchangeName, emailDlqRoutingKey);
        return QueueBuilder.durable(emailQueueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", emailDlqRoutingKey)
                .build();
    }

    @Bean
    public Queue pushQueue() {
        logger.info("Creating Queue: {} with DLX: {} and DLQ routing key: {}", pushQueueName, deadLetterExchangeName, pushDlqRoutingKey);
        return QueueBuilder.durable(pushQueueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", pushDlqRoutingKey)
                .build();
    }

    // --- Dead Letter Queues (DLQs) ---
    @Bean
    public Queue emailDlq() {
        logger.info("Creating DLQ: {}", emailDlqName);
        return QueueBuilder.durable(emailDlqName).build();
    }

    @Bean
    public Queue pushDlq() {
        logger.info("Creating DLQ: {}", pushDlqName);
        return QueueBuilder.durable(pushDlqName).build();
    }

    // === Bindings ===
    @Bean
    public Binding emailBinding(TopicExchange notificationExchange, Queue emailQueue) {
        logger.info("Binding Queue {} to Exchange {} with RoutingKey {}", emailQueue.getName(), notificationExchange.getName(), emailRoutingKey);
        return BindingBuilder.bind(emailQueue).to(notificationExchange).with(emailRoutingKey);
    }

    @Bean
    public Binding pushBinding(TopicExchange notificationExchange, Queue pushQueue) {
        logger.info("Binding Queue {} to Exchange {} with RoutingKey {}", pushQueue.getName(), notificationExchange.getName(), pushRoutingKey);
        return BindingBuilder.bind(pushQueue).to(notificationExchange).with(pushRoutingKey);
    }

    // --- Bindings for DLQs to Dead Letter Exchange ---
    @Bean
    public Binding emailDlqBinding(DirectExchange deadLetterExchange, Queue emailDlq) {
        logger.info("Binding DLQ {} to DLX {} with RoutingKey {}", emailDlq.getName(), deadLetterExchange.getName(), emailDlqRoutingKey);
        return BindingBuilder.bind(emailDlq).to(deadLetterExchange).with(emailDlqRoutingKey);
    }

    @Bean
    public Binding pushDlqBinding(DirectExchange deadLetterExchange, Queue pushDlq) {
        logger.info("Binding DLQ {} to DLX {} with RoutingKey {}", pushDlq.getName(), deadLetterExchange.getName(), pushDlqRoutingKey);
        return BindingBuilder.bind(pushDlq).to(deadLetterExchange).with(pushDlqRoutingKey);
    }

    // === RabbitTemplate Configuration ===
    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());

        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (correlationData != null) {
                logger.info("Publisher Confirm: CorrelationData ID: {}, Ack: {}, Cause: {}", correlationData.getId(), ack, cause);
            } else {
                logger.info("Publisher Confirm: No CorrelationData, Ack: {}, Cause: {}", ack, cause);
            }
            if (!ack) {
                logger.error("Message failed to be confirmed by broker. Cause: {}", cause);
            }
        });

        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setReturnsCallback(returned -> {
            logger.error("Message returned. Message: {}, ReplyCode: {}, ReplyText: {}, Exchange: {}, RoutingKey: {}",
                    new String(returned.getMessage().getBody()),
                    returned.getReplyCode(),
                    returned.getReplyText(),
                    returned.getExchange(),
                    returned.getRoutingKey());
        });
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
