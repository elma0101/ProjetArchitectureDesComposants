package com.bookstore.loanmanagement.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for loan management service
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "bookstore.events";
    public static final String LOAN_CREATED_QUEUE = "loan.created";
    public static final String LOAN_RETURNED_QUEUE = "loan.returned";
    public static final String BOOK_AVAILABILITY_QUEUE = "loan.book.availability";
    public static final String SAGA_COMPENSATION_QUEUE = "loan.saga.compensation";

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
     * Declare loan.created queue
     */
    @Bean
    public Queue loanCreatedQueue() {
        return QueueBuilder.durable(LOAN_CREATED_QUEUE)
            .withArgument("x-message-ttl", 86400000) // 24 hours
            .withArgument("x-max-length", 10000)
            .build();
    }

    /**
     * Declare loan.returned queue
     */
    @Bean
    public Queue loanReturnedQueue() {
        return QueueBuilder.durable(LOAN_RETURNED_QUEUE)
            .withArgument("x-message-ttl", 86400000)
            .withArgument("x-max-length", 10000)
            .build();
    }

    /**
     * Declare book availability queue for loan service
     */
    @Bean
    public Queue bookAvailabilityQueue() {
        return QueueBuilder.durable(BOOK_AVAILABILITY_QUEUE)
            .withArgument("x-message-ttl", 86400000)
            .withArgument("x-max-length", 10000)
            .build();
    }

    /**
     * Declare saga compensation queue
     */
    @Bean
    public Queue sagaCompensationQueue() {
        return QueueBuilder.durable(SAGA_COMPENSATION_QUEUE)
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
     * Bind book availability queue to exchange with wildcard pattern
     */
    @Bean
    public Binding bookAvailabilityBinding(Queue bookAvailabilityQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(bookAvailabilityQueue)
            .to(bookstoreExchange)
            .with("book.availability.*");
    }

    /**
     * Bind saga compensation queue to exchange
     */
    @Bean
    public Binding sagaCompensationBinding(Queue sagaCompensationQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(sagaCompensationQueue)
            .to(bookstoreExchange)
            .with("loan.saga.compensation");
    }
}
