package com.bookstore.notification.service;

import com.bookstore.notification.dto.NotificationRequest;
import com.bookstore.notification.entity.NotificationType;
import com.bookstore.notification.event.LoanEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for listening to loan-related events and sending notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanEventListener {

    private final NotificationService notificationService;

    /**
     * Handle loan created events
     */
    @RabbitListener(queues = "notification.loan.created")
    public void handleLoanCreated(LoanEvent event) {
        log.info("Received loan created event for loan: {}", event.getLoanId());

        try {
            String subject = "Loan Confirmation - " + event.getBookTitle();
            String content = String.format(
                    "Dear %s,\n\n" +
                    "Your loan request has been confirmed.\n\n" +
                    "Book: %s\n" +
                    "Loan Date: %s\n" +
                    "Due Date: %s\n\n" +
                    "Please return the book by the due date to avoid late fees.\n\n" +
                    "Thank you for using our library service!",
                    event.getUserName(),
                    event.getBookTitle(),
                    event.getLoanDate(),
                    event.getDueDate()
            );

            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getUserId())
                    .type(NotificationType.EMAIL)
                    .recipient(event.getUserEmail())
                    .subject(subject)
                    .content(content)
                    .build();

            notificationService.sendNotification(request);
            log.info("Loan created notification sent for loan: {}", event.getLoanId());
        } catch (Exception e) {
            log.error("Failed to send loan created notification for loan: {}", event.getLoanId(), e);
        }
    }

    /**
     * Handle loan returned events
     */
    @RabbitListener(queues = "notification.loan.returned")
    public void handleLoanReturned(LoanEvent event) {
        log.info("Received loan returned event for loan: {}", event.getLoanId());

        try {
            String subject = "Loan Return Confirmation - " + event.getBookTitle();
            String content = String.format(
                    "Dear %s,\n\n" +
                    "Thank you for returning the book.\n\n" +
                    "Book: %s\n" +
                    "Return Date: %s\n\n" +
                    "We hope you enjoyed reading it!\n\n" +
                    "Thank you for using our library service!",
                    event.getUserName(),
                    event.getBookTitle(),
                    event.getReturnDate()
            );

            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getUserId())
                    .type(NotificationType.EMAIL)
                    .recipient(event.getUserEmail())
                    .subject(subject)
                    .content(content)
                    .build();

            notificationService.sendNotification(request);
            log.info("Loan returned notification sent for loan: {}", event.getLoanId());
        } catch (Exception e) {
            log.error("Failed to send loan returned notification for loan: {}", event.getLoanId(), e);
        }
    }

    /**
     * Handle loan overdue events
     */
    @RabbitListener(queues = "notification.loan.overdue")
    public void handleLoanOverdue(LoanEvent event) {
        log.info("Received loan overdue event for loan: {}", event.getLoanId());

        try {
            String subject = "Overdue Loan Notice - " + event.getBookTitle();
            String content = String.format(
                    "Dear %s,\n\n" +
                    "This is a reminder that your loan is overdue.\n\n" +
                    "Book: %s\n" +
                    "Due Date: %s\n\n" +
                    "Please return the book as soon as possible to avoid additional late fees.\n\n" +
                    "If you have already returned the book, please disregard this notice.",
                    event.getUserName(),
                    event.getBookTitle(),
                    event.getDueDate()
            );

            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getUserId())
                    .type(NotificationType.EMAIL)
                    .recipient(event.getUserEmail())
                    .subject(subject)
                    .content(content)
                    .build();

            notificationService.sendNotification(request);
            log.info("Loan overdue notification sent for loan: {}", event.getLoanId());
        } catch (Exception e) {
            log.error("Failed to send loan overdue notification for loan: {}", event.getLoanId(), e);
        }
    }

    /**
     * Handle loan due soon events
     */
    @RabbitListener(queues = "notification.loan.due-soon")
    public void handleLoanDueSoon(LoanEvent event) {
        log.info("Received loan due soon event for loan: {}", event.getLoanId());

        try {
            String subject = "Loan Due Soon Reminder - " + event.getBookTitle();
            String content = String.format(
                    "Dear %s,\n\n" +
                    "This is a friendly reminder that your loan is due soon.\n\n" +
                    "Book: %s\n" +
                    "Due Date: %s\n\n" +
                    "Please return the book by the due date to avoid late fees.\n\n" +
                    "Thank you for using our library service!",
                    event.getUserName(),
                    event.getBookTitle(),
                    event.getDueDate()
            );

            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getUserId())
                    .type(NotificationType.EMAIL)
                    .recipient(event.getUserEmail())
                    .subject(subject)
                    .content(content)
                    .build();

            notificationService.sendNotification(request);
            log.info("Loan due soon notification sent for loan: {}", event.getLoanId());
        } catch (Exception e) {
            log.error("Failed to send loan due soon notification for loan: {}", event.getLoanId(), e);
        }
    }
}
