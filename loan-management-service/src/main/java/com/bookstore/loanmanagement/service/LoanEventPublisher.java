package com.bookstore.loanmanagement.service;

import com.bookstore.loanmanagement.event.LoanCreatedEvent;
import com.bookstore.loanmanagement.event.LoanReturnedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for publishing loan-related events to RabbitMQ
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanEventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    private static final String EXCHANGE_NAME = "bookstore.events";
    private static final String LOAN_CREATED_ROUTING_KEY = "loan.created";
    private static final String LOAN_RETURNED_ROUTING_KEY = "loan.returned";
    
    /**
     * Publish loan created event
     */
    public void publishLoanCreated(LoanCreatedEvent event) {
        try {
            log.info("Publishing loan created event: loanId={}, correlationId={}", 
                    event.getLoanId(), event.getCorrelationId());
            
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, LOAN_CREATED_ROUTING_KEY, event);
            
            log.debug("Loan created event published successfully: eventId={}", event.getEventId());
        } catch (Exception e) {
            log.error("Failed to publish loan created event: loanId={}, error={}", 
                    event.getLoanId(), e.getMessage(), e);
            // Don't throw exception - event publishing failure shouldn't break the flow
        }
    }
    
    /**
     * Publish loan returned event
     */
    public void publishLoanReturned(LoanReturnedEvent event) {
        try {
            log.info("Publishing loan returned event: loanId={}, correlationId={}", 
                    event.getLoanId(), event.getCorrelationId());
            
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, LOAN_RETURNED_ROUTING_KEY, event);
            
            log.debug("Loan returned event published successfully: eventId={}", event.getEventId());
        } catch (Exception e) {
            log.error("Failed to publish loan returned event: loanId={}, error={}", 
                    event.getLoanId(), e.getMessage(), e);
            // Don't throw exception - event publishing failure shouldn't break the flow
        }
    }
}
