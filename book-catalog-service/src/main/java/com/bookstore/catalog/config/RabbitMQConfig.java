package com.bookstore.catalog.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for book catalog service
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "bookstore.events";
    public static final String BOOK_CREATED_QUEUE = "book.created";
    public static final String BOOK_UPDATED_QUEUE = "book.updated";
    public static final String BOOK_DELETED_QUEUE = "book.deleted";
    public static final String BOOK_AVAILABILITY_QUEUE = "book.availability.changed";

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
     * Declare the main exchange (if not already declared by RabbitMQ definitions)
     */
    @Bean
    public TopicExchange bookstoreExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    /**
     * Declare book.created queue
     */
    @Bean
    public Queue bookCreatedQueue() {
        return QueueBuilder.durable(BOOK_CREATED_QUEUE)
            .withArgument("x-message-ttl", 86400000) // 24 hours
            .withArgument("x-max-length", 10000)
            .build();
    }

    /**
     * Declare book.updated queue
     */
    @Bean
    public Queue bookUpdatedQueue() {
        return QueueBuilder.durable(BOOK_UPDATED_QUEUE)
            .withArgument("x-message-ttl", 86400000)
            .withArgument("x-max-length", 10000)
            .build();
    }

    /**
     * Declare book.deleted queue
     */
    @Bean
    public Queue bookDeletedQueue() {
        return QueueBuilder.durable(BOOK_DELETED_QUEUE)
            .withArgument("x-message-ttl", 86400000)
            .withArgument("x-max-length", 10000)
            .build();
    }

    /**
     * Declare book.availability.changed queue
     */
    @Bean
    public Queue bookAvailabilityQueue() {
        return QueueBuilder.durable(BOOK_AVAILABILITY_QUEUE)
            .withArgument("x-message-ttl", 86400000)
            .withArgument("x-max-length", 10000)
            .build();
    }

    /**
     * Bind book.created queue to exchange
     */
    @Bean
    public Binding bookCreatedBinding(Queue bookCreatedQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(bookCreatedQueue)
            .to(bookstoreExchange)
            .with("book.created");
    }

    /**
     * Bind book.updated queue to exchange
     */
    @Bean
    public Binding bookUpdatedBinding(Queue bookUpdatedQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(bookUpdatedQueue)
            .to(bookstoreExchange)
            .with("book.updated");
    }

    /**
     * Bind book.deleted queue to exchange
     */
    @Bean
    public Binding bookDeletedBinding(Queue bookDeletedQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(bookDeletedQueue)
            .to(bookstoreExchange)
            .with("book.deleted");
    }

    /**
     * Bind book.availability.changed queue to exchange with wildcard pattern
     */
    @Bean
    public Binding bookAvailabilityBinding(Queue bookAvailabilityQueue, TopicExchange bookstoreExchange) {
        return BindingBuilder.bind(bookAvailabilityQueue)
            .to(bookstoreExchange)
            .with("book.availability.*");
    }
}
