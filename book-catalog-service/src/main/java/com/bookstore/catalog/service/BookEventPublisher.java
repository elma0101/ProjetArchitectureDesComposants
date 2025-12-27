package com.bookstore.catalog.service;

import com.bookstore.catalog.event.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for publishing book-related events to RabbitMQ
 */
@Service
public class BookEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(BookEventPublisher.class);
    private static final String EXCHANGE_NAME = "bookstore.events";
    private static final String CORRELATION_ID_KEY = "correlationId";

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public BookEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Publish book created event
     */
    public void publishBookCreated(BookCreatedEvent event) {
        String correlationId = getOrCreateCorrelationId();
        event.setCorrelationId(correlationId);
        
        publishEvent("book.created", event);
        logger.info("Published BookCreatedEvent for book ID: {} with correlation ID: {}", 
                   event.getBookId(), correlationId);
    }

    /**
     * Publish book updated event
     */
    public void publishBookUpdated(BookUpdatedEvent event) {
        String correlationId = getOrCreateCorrelationId();
        event.setCorrelationId(correlationId);
        
        publishEvent("book.updated", event);
        logger.info("Published BookUpdatedEvent for book ID: {} with correlation ID: {}", 
                   event.getBookId(), correlationId);
    }

    /**
     * Publish book deleted event
     */
    public void publishBookDeleted(BookDeletedEvent event) {
        String correlationId = getOrCreateCorrelationId();
        event.setCorrelationId(correlationId);
        
        publishEvent("book.deleted", event);
        logger.info("Published BookDeletedEvent for book ID: {} with correlation ID: {}", 
                   event.getBookId(), correlationId);
    }

    /**
     * Publish book availability changed event
     */
    public void publishBookAvailabilityChanged(BookAvailabilityChangedEvent event) {
        String correlationId = getOrCreateCorrelationId();
        event.setCorrelationId(correlationId);
        
        publishEvent("book.availability.changed", event);
        logger.info("Published BookAvailabilityChangedEvent for book ID: {} with correlation ID: {}", 
                   event.getBookId(), correlationId);
    }

    /**
     * Generic method to publish events to RabbitMQ
     */
    private void publishEvent(String routingKey, BookEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            
            rabbitTemplate.convertAndSend(
                EXCHANGE_NAME,
                routingKey,
                eventJson,
                message -> {
                    message.getMessageProperties().setCorrelationId(event.getCorrelationId());
                    message.getMessageProperties().setContentType("application/json");
                    return message;
                }
            );
            
            logger.debug("Event published to exchange: {}, routing key: {}, event: {}", 
                        EXCHANGE_NAME, routingKey, event.getEventType());
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize event: {}", event.getEventType(), e);
            throw new RuntimeException("Failed to publish event", e);
        } catch (Exception e) {
            logger.error("Failed to publish event to RabbitMQ: {}", event.getEventType(), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    /**
     * Get correlation ID from MDC or create a new one
     */
    private String getOrCreateCorrelationId() {
        String correlationId = MDC.get(CORRELATION_ID_KEY);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = java.util.UUID.randomUUID().toString();
            MDC.put(CORRELATION_ID_KEY, correlationId);
        }
        return correlationId;
    }
}
