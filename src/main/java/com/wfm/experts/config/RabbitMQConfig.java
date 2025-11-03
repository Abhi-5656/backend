package com.wfm.experts.config;

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

    @Value("${rabbitmq.queue.in_app}")
    private String inAppQueueName;

    @Value("${rabbitmq.queue.punch_processing}")
    private String punchProcessingQueueName;

    @Value("${rabbitmq.queue.leave_balance_recalculation}")
    private String leaveBalanceRecalculationQueueName;

    @Value("${rabbitmq.queue.employee_location}") // <-- NEW
    private String employeeLocationQueueName; // <-- NEW

    @Value("${rabbitmq.queue.dlq.email}")
    private String emailDlqName;

    @Value("${rabbitmq.queue.dlq.push}")
    private String pushDlqName;

    @Value("${rabbitmq.queue.dlq.in_app}")
    private String inAppDlqName;

    @Value("${rabbitmq.queue.dlq.punch_processing}")
    private String punchProcessingDlqName;

    @Value("${rabbitmq.queue.dlq.leave_balance_recalculation}")
    private String leaveBalanceRecalculationDlqName;

    @Value("${rabbitmq.queue.dlq.employee_location}") // <-- NEW
    private String employeeLocationDlqName; // <-- NEW

    // --- Routing Keys ---
    @Value("${rabbitmq.routingkey.email}")
    private String emailRoutingKey;

    @Value("${rabbitmq.routingkey.push}")
    private String pushRoutingKey;

    @Value("${rabbitmq.routingkey.in_app}")
    private String inAppRoutingKey;

    @Value("${rabbitmq.routingkey.punch_processing}")
    private String punchProcessingRoutingKey;

    @Value("${rabbitmq.routingkey.leave_balance_recalculation}")
    private String leaveBalanceRecalculationRoutingKey;

    @Value("${rabbitmq.routingkey.employee_location}") // <-- NEW
    private String employeeLocationRoutingKey; // <-- NEW

    // Routing keys for DLQs
    @Value("${rabbitmq.routingkey.dlq.email}")
    private String emailDlqRoutingKey;

    @Value("${rabbitmq.routingkey.dlq.push}")
    private String pushDlqRoutingKey;

    @Value("${rabbitmq.routingkey.dlq.in_app}")
    private String inAppDlqRoutingKey;

    @Value("${rabbitmq.routingkey.dlq.punch_processing}")
    private String punchProcessingDlqRoutingKey;

    @Value("${rabbitmq.routingkey.dlq.leave_balance_recalculation}")
    private String leaveBalanceRecalculationDlqRoutingKey;

    @Value("${rabbitmq.routingkey.dlq.employee_location}") // <-- NEW
    private String employeeLocationDlqRoutingKey; // <-- NEW

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
        logger.info("Creating Email Queue: {} with DLX: {} and DLQ routing key: {}", emailQueueName, deadLetterExchangeName, emailDlqRoutingKey);
        return QueueBuilder.durable(emailQueueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", emailDlqRoutingKey)
                .build();
    }

    @Bean
    public Queue pushQueue() {
        logger.info("Creating Push Queue: {} with DLX: {} and DLQ routing key: {}", pushQueueName, deadLetterExchangeName, pushDlqRoutingKey);
        return QueueBuilder.durable(pushQueueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", pushDlqRoutingKey)
                .build();
    }

    @Bean
    public Queue inAppQueue() {
        logger.info("Creating In-App Queue: {} with DLX: {} and DLQ routing key: {}", inAppQueueName, deadLetterExchangeName, inAppDlqRoutingKey);
        return QueueBuilder.durable(inAppQueueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", inAppDlqRoutingKey)
                .build();
    }

    @Bean
    public Queue punchProcessingQueue() {
        logger.info("Creating Punch Processing Queue: {} with DLX: {} and DLQ routing key: {}", punchProcessingQueueName, deadLetterExchangeName, punchProcessingDlqRoutingKey);
        return QueueBuilder.durable(punchProcessingQueueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", punchProcessingDlqRoutingKey)
                .build();
    }

    @Bean
    public Queue leaveBalanceRecalculationQueue() {
        logger.info("Creating Leave Balance Recalculation Queue: {} with DLX: {} and DLQ routing key: {}", leaveBalanceRecalculationQueueName, deadLetterExchangeName, leaveBalanceRecalculationDlqRoutingKey);
        return QueueBuilder.durable(leaveBalanceRecalculationQueueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", leaveBalanceRecalculationDlqRoutingKey)
                .build();
    }

    // <-- NEW QUEUE -->
    @Bean
    public Queue employeeLocationQueue() {
        logger.info("Creating Employee Location Queue: {} with DLX: {} and DLQ routing key: {}", employeeLocationQueueName, deadLetterExchangeName, employeeLocationDlqRoutingKey);
        return QueueBuilder.durable(employeeLocationQueueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", employeeLocationDlqRoutingKey)
                .build();
    }

    // --- Dead Letter Queues (DLQs) ---
    @Bean
    public Queue emailDlq() {
        logger.info("Creating Email DLQ: {}", emailDlqName);
        return QueueBuilder.durable(emailDlqName).build();
    }

    @Bean
    public Queue pushDlq() {
        logger.info("Creating Push DLQ: {}", pushDlqName);
        return QueueBuilder.durable(pushDlqName).build();
    }

    @Bean
    public Queue inAppDlq() {
        logger.info("Creating In-App DLQ: {}", inAppDlqName);
        return QueueBuilder.durable(inAppDlqName).build();
    }

    @Bean
    public Queue punchProcessingDlq() {
        logger.info("Creating Punch Processing DLQ: {}", punchProcessingDlqName);
        return QueueBuilder.durable(punchProcessingDlqName).build();
    }

    @Bean
    public Queue leaveBalanceRecalculationDlq() {
        logger.info("Creating Leave Balance Recalculation DLQ: {}", leaveBalanceRecalculationDlqName);
        return QueueBuilder.durable(leaveBalanceRecalculationDlqName).build();
    }

    // <-- NEW DLQ -->
    @Bean
    public Queue employeeLocationDlq() {
        logger.info("Creating Employee Location DLQ: {}", employeeLocationDlqName);
        return QueueBuilder.durable(employeeLocationDlqName).build();
    }


    // === Bindings ===
    @Bean
    public Binding emailBinding(TopicExchange notificationExchange, Queue emailQueue) {
        logger.info("Binding Email Queue {} to Exchange {} with RoutingKey {}", emailQueue.getName(), notificationExchange.getName(), emailRoutingKey);
        return BindingBuilder.bind(emailQueue).to(notificationExchange).with(emailRoutingKey);
    }

    @Bean
    public Binding pushBinding(TopicExchange notificationExchange, Queue pushQueue) {
        logger.info("Binding Push Queue {} to Exchange {} with RoutingKey {}", pushQueue.getName(), notificationExchange.getName(), pushRoutingKey);
        return BindingBuilder.bind(pushQueue).to(notificationExchange).with(pushRoutingKey);
    }

    @Bean
    public Binding inAppBinding(TopicExchange notificationExchange, Queue inAppQueue) {
        logger.info("Binding In-App Queue {} to Exchange {} with RoutingKey {}", inAppQueue.getName(), notificationExchange.getName(), inAppRoutingKey);
        return BindingBuilder.bind(inAppQueue).to(notificationExchange).with(inAppRoutingKey);
    }

    @Bean
    public Binding punchProcessingBinding(TopicExchange notificationExchange, Queue punchProcessingQueue) {
        logger.info("Binding Punch Processing Queue {} to Exchange {} with RoutingKey {}", punchProcessingQueue.getName(), notificationExchange.getName(), punchProcessingRoutingKey);
        return BindingBuilder.bind(punchProcessingQueue).to(notificationExchange).with(punchProcessingRoutingKey);
    }

    @Bean
    public Binding leaveBalanceRecalculationBinding(TopicExchange notificationExchange, Queue leaveBalanceRecalculationQueue) {
        logger.info("Binding Leave Balance Recalculation Queue {} to Exchange {} with RoutingKey {}", leaveBalanceRecalculationQueue.getName(), notificationExchange.getName(), leaveBalanceRecalculationRoutingKey);
        return BindingBuilder.bind(leaveBalanceRecalculationQueue).to(notificationExchange).with(leaveBalanceRecalculationRoutingKey);
    }

    // <-- NEW BINDING -->
    @Bean
    public Binding employeeLocationBinding(TopicExchange notificationExchange, Queue employeeLocationQueue) {
        logger.info("Binding Employee Location Queue {} to Exchange {} with RoutingKey {}", employeeLocationQueue.getName(), notificationExchange.getName(), employeeLocationRoutingKey);
        return BindingBuilder.bind(employeeLocationQueue).to(notificationExchange).with(employeeLocationRoutingKey);
    }


    // --- Bindings for DLQs to Dead Letter Exchange ---
    @Bean
    public Binding emailDlqBinding(DirectExchange deadLetterExchange, Queue emailDlq) {
        logger.info("Binding Email DLQ {} to DLX {} with RoutingKey {}", emailDlq.getName(), deadLetterExchange.getName(), emailDlqRoutingKey);
        return BindingBuilder.bind(emailDlq).to(deadLetterExchange).with(emailDlqRoutingKey);
    }

    @Bean
    public Binding pushDlqBinding(DirectExchange deadLetterExchange, Queue pushDlq) {
        logger.info("Binding Push DLQ {} to DLX {} with RoutingKey {}", pushDlq.getName(), deadLetterExchange.getName(), pushDlqRoutingKey);
        return BindingBuilder.bind(pushDlq).to(deadLetterExchange).with(pushDlqRoutingKey);
    }

    @Bean
    public Binding inAppDlqBinding(DirectExchange deadLetterExchange, Queue inAppDlq) {
        logger.info("Binding In-App DLQ {} to DLX {} with RoutingKey {}", inAppDlq.getName(), deadLetterExchange.getName(), inAppDlqRoutingKey);
        return BindingBuilder.bind(inAppDlq).to(deadLetterExchange).with(inAppDlqRoutingKey);
    }

    @Bean
    public Binding punchProcessingDlqBinding(DirectExchange deadLetterExchange, Queue punchProcessingDlq) {
        logger.info("Binding Punch Processing DLQ {} to DLX {} with RoutingKey {}", punchProcessingDlq.getName(), deadLetterExchange.getName(), punchProcessingDlqRoutingKey);
        return BindingBuilder.bind(punchProcessingDlq).to(deadLetterExchange).with(punchProcessingDlqRoutingKey);
    }

    @Bean
    public Binding leaveBalanceRecalculationDlqBinding(DirectExchange deadLetterExchange, Queue leaveBalanceRecalculationDlq) {
        logger.info("Binding Leave Balance Recalculation DLQ {} to DLX {} with RoutingKey {}", leaveBalanceRecalculationDlq.getName(), deadLetterExchange.getName(), leaveBalanceRecalculationDlqRoutingKey);
        return BindingBuilder.bind(leaveBalanceRecalculationDlq).to(deadLetterExchange).with(leaveBalanceRecalculationDlqRoutingKey);
    }

    // <-- NEW DLQ BINDING -->
    @Bean
    public Binding employeeLocationDlqBinding(DirectExchange deadLetterExchange, Queue employeeLocationDlq) {
        logger.info("Binding Employee Location DLQ {} to DLX {} with RoutingKey {}", employeeLocationDlq.getName(), deadLetterExchange.getName(), employeeLocationDlqRoutingKey);
        return BindingBuilder.bind(employeeLocationDlq).to(deadLetterExchange).with(employeeLocationDlqRoutingKey);
    }


    // === RabbitTemplate Configuration (remains the same) ===
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