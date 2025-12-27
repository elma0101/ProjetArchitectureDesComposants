package com.bookstore.recommendation.service;

import com.bookstore.recommendation.event.LoanEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanEventListener {
    
    private final AnalyticsService analyticsService;
    private final UserPreferenceService userPreferenceService;
    private final ObjectMapper objectMapper;
    
    @RabbitListener(queues = "${rabbitmq.queue.loan-events}")
    public void handleLoanEvent(String message) {
        try {
            log.info("Received loan event: {}", message);
            
            LoanEvent event = objectMapper.readValue(message, LoanEvent.class);
            
            switch (event.getEventType()) {
                case "BORROWED" -> handleBorrowEvent(event);
                case "RETURNED" -> handleReturnEvent(event);
                default -> log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing loan event", e);
        }
    }
    
    private void handleBorrowEvent(LoanEvent event) {
        log.info("Processing borrow event for user: {}, book: {}", 
            event.getUserId(), event.getBookId());
        
        // Update analytics
        analyticsService.recordBorrow(event.getBookId());
        
        // Update user preferences
        userPreferenceService.recordBorrow(event.getUserId(), event.getBookId());
    }
    
    private void handleReturnEvent(LoanEvent event) {
        log.info("Processing return event for user: {}, book: {}", 
            event.getUserId(), event.getBookId());
        
        // Update analytics
        analyticsService.recordReturn(event.getBookId());
    }
}
