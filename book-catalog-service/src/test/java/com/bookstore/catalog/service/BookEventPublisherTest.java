package com.bookstore.catalog.service;

import com.bookstore.catalog.event.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private BookEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        eventPublisher = new BookEventPublisher(rabbitTemplate);
    }

    @Test
    void publishBookCreated_shouldPublishEventWithCorrectRoutingKey() {
        // Given
        BookCreatedEvent event = new BookCreatedEvent(
            1L, "978-0-123456-78-9", "Test Book", "Description",
            2024, "Fiction", 10, 10, Set.of(1L), "corr-123"
        );

        // When
        eventPublisher.publishBookCreated(event);

        // Then
        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(rabbitTemplate).convertAndSend(
            exchangeCaptor.capture(),
            routingKeyCaptor.capture(),
            messageCaptor.capture(),
            any(org.springframework.amqp.core.MessagePostProcessor.class)
        );

        assertThat(exchangeCaptor.getValue()).isEqualTo("bookstore.events");
        assertThat(routingKeyCaptor.getValue()).isEqualTo("book.created");
        assertThat(messageCaptor.getValue()).contains("Test Book");
        assertThat(messageCaptor.getValue()).contains("978-0-123456-78-9");
    }

    @Test
    void publishBookUpdated_shouldPublishEventWithCorrectRoutingKey() {
        // Given
        BookUpdatedEvent event = new BookUpdatedEvent(
            1L, "978-0-123456-78-9", "Updated Book", "New Description",
            2024, "Fiction", 10, 8, Set.of(1L), 10, "corr-123"
        );

        // When
        eventPublisher.publishBookUpdated(event);

        // Then
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);

        verify(rabbitTemplate).convertAndSend(
            eq("bookstore.events"),
            routingKeyCaptor.capture(),
            anyString(),
            any(org.springframework.amqp.core.MessagePostProcessor.class)
        );

        assertThat(routingKeyCaptor.getValue()).isEqualTo("book.updated");
    }

    @Test
    void publishBookDeleted_shouldPublishEventWithCorrectRoutingKey() {
        // Given
        BookDeletedEvent event = new BookDeletedEvent(
            1L, "978-0-123456-78-9", "Deleted Book", "corr-123"
        );

        // When
        eventPublisher.publishBookDeleted(event);

        // Then
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);

        verify(rabbitTemplate).convertAndSend(
            eq("bookstore.events"),
            routingKeyCaptor.capture(),
            anyString(),
            any(org.springframework.amqp.core.MessagePostProcessor.class)
        );

        assertThat(routingKeyCaptor.getValue()).isEqualTo("book.deleted");
    }

    @Test
    void publishBookAvailabilityChanged_shouldPublishEventWithCorrectRoutingKey() {
        // Given
        BookAvailabilityChangedEvent event = new BookAvailabilityChangedEvent(
            1L, "978-0-123456-78-9", "Test Book",
            10, 8, 10, "Loan created", "corr-123"
        );

        // When
        eventPublisher.publishBookAvailabilityChanged(event);

        // Then
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);

        verify(rabbitTemplate).convertAndSend(
            eq("bookstore.events"),
            routingKeyCaptor.capture(),
            anyString(),
            any(org.springframework.amqp.core.MessagePostProcessor.class)
        );

        assertThat(routingKeyCaptor.getValue()).isEqualTo("book.availability.changed");
    }

    @Test
    void publishEvent_shouldSetCorrelationIdInMessageProperties() {
        // Given
        BookCreatedEvent event = new BookCreatedEvent(
            1L, "978-0-123456-78-9", "Test Book", "Description",
            2024, "Fiction", 10, 10, Set.of(1L), null
        );

        // When
        eventPublisher.publishBookCreated(event);

        // Then
        assertThat(event.getCorrelationId()).isNotNull();
        assertThat(event.getCorrelationId()).isNotEmpty();
    }
}
