package com.bookstore.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for notification service
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "bookstore.events";
    public static final String LOAN_CREATED_QUEUE = "notification.loan.created";
    public static final String LOAN_RETURNED_QUEUE = "notification.loan.returned";
    public static final String LOAN_OVERDUE_QUEUE = "notification.loan.overdue";
    public static final String LOAN_DUE_SOON_QUEUE = "notification.loan.due-soon";

    /**
     * Configure message converter to use Jackson for JSON serialization
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configure RabbitTemplate with JSON message converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    /**
     * Declare the main exchange (if not already declared)
     */
    @Bean
    public TopicExchange bookstoreExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    /**
     * Declare loan.created queue for notifications
     */
    @Bean
    public Queue loanCreatedQueue() {
        return QueueBuilder.durable(LOAN_CREATED_QUEUE)
                .withArgument("x-message-ttl", 86400000) // 24 hours
                .withArgument("x-max-length", 10000)
                .build();
    }

    /**
     * Declare loan.returned queue for notifications
     */
    @Bean
    public Queue loanReturnedQueue() {
        return QueueBuilder.durable(LOAN_RETURNED_QUEUE)
                .withArgument("x-message-ttl", 86400000)
                .withArgument("x-max-length", 10000)
                .build();
    }

    /**
     * Declare loan.overdue queue for notifications
     */
    @Bean
    public Queue loanOverdueQueue() {
        return QueueBuilder.durable(LOAN_OVERDUE_QUEUE)
                .withArgument("x-message-ttl", 86400000)
                .withArgument("x-max-length", 10000)
                .build();
    }

    /**
     * Declare loan.due-soon queue for notifications
     */
    @Bean
    public Queue loanDueSoonQueue() {
        return QueueBuilder.durable(LOAN_DUE_SOON_QUEUE)
                .withArgument("x-message-ttl", 86400000)
                .withArgument("x-max-length", 10000)
                .build();
    }

    /**
     * Bind loan.created queue to exchange
     */
    @Bean
    public Binding loanCreatedBinding(Queue loanCreatedQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(loanCreatedQueue)
                .to(bookstoreExchange)
                .with("loan.created");
    }

    /**
     * Bind loan.returned queue to exchange
     */
    @Bean
    public Binding loanReturnedBinding(Queue loanReturnedQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(loanReturnedQueue)
                .to(bookstoreExchange)
                .with("loan.returned");
    }

    /**
     * Bind loan.overdue queue to exchange
     */
    @Bean
    public Binding loanOverdueBinding(Queue loanOverdueQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(loanOverdueQueue)
                .to(bookstoreExchange)
                .with("loan.overdue");
    }

    /**
     * Bind loan.due-soon queue to exchange
     */
    @Bean
    public Binding loanDueSoonBinding(Queue loanDueSoonQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(loanDueSoonQueue)
                .to(bookstoreExchange)
                .with("loan.due-soon");
    }
}
