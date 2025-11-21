package com.bookstore.service;

import com.bookstore.entity.Loan;
import com.bookstore.entity.LoanStatus;
import com.bookstore.repository.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for handling loan notifications and automated tracking
 */
@Service
@Transactional
public class LoanNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(LoanNotificationService.class);
    
    private final LoanRepository loanRepository;
    private final EmailNotificationService emailNotificationService;
    private final LoanTrackingService loanTrackingService;
    
    @Autowired
    public LoanNotificationService(LoanRepository loanRepository, 
                                 EmailNotificationService emailNotificationService,
                                 LoanTrackingService loanTrackingService) {
        this.loanRepository = loanRepository;
        this.emailNotificationService = emailNotificationService;
        this.loanTrackingService = loanTrackingService;
    }
    
    /**
     * Scheduled task to check for overdue loans and send notifications
     * Runs daily at 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void processOverdueLoans() {
        logger.info("Starting overdue loan processing...");
        
        try {
            // Update loan statuses to overdue
            List<Loan> overdueLoans = updateOverdueLoans();
            
            // Send overdue notifications
            for (Loan loan : overdueLoans) {
                sendOverdueNotification(loan);
                loanTrackingService.recordNotificationSent(loan.getId(), "OVERDUE_NOTIFICATION");
            }
            
            logger.info("Processed {} overdue loans", overdueLoans.size());
        } catch (Exception e) {
            logger.error("Error processing overdue loans", e);
        }
    }
    
    /**
     * Scheduled task to send reminder notifications for loans due soon
     * Runs daily at 8:00 AM
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendDueReminders() {
        logger.info("Starting due reminder processing...");
        
        try {
            // Send reminders for loans due in 3 days
            List<Loan> loansDueSoon = loanRepository.findLoansDueWithinDays(LocalDate.now().plusDays(3));
            
            for (Loan loan : loansDueSoon) {
                // Check if reminder was already sent today
                if (!loanTrackingService.wasNotificationSentToday(loan.getId(), "DUE_REMINDER")) {
                    sendDueReminderNotification(loan);
                    loanTrackingService.recordNotificationSent(loan.getId(), "DUE_REMINDER");
                }
            }
            
            logger.info("Sent due reminders for {} loans", loansDueSoon.size());
        } catch (Exception e) {
            logger.error("Error sending due reminders", e);
        }
    }
    
    /**
     * Update loans that are past due date to overdue status
     */
    private List<Loan> updateOverdueLoans() {
        List<Loan> activeLoans = loanRepository.findByStatus(LoanStatus.ACTIVE, org.springframework.data.domain.Pageable.unpaged()).getContent();
        List<Loan> newlyOverdueLoans = new java.util.ArrayList<>();
        
        for (Loan loan : activeLoans) {
            if (loan.isOverdue() && loan.getStatus() == LoanStatus.ACTIVE) {
                loan.markAsOverdue();
                loanRepository.save(loan);
                newlyOverdueLoans.add(loan);
                
                // Record status change
                loanTrackingService.recordStatusChange(loan.getId(), LoanStatus.ACTIVE, LoanStatus.OVERDUE);
            }
        }
        
        return newlyOverdueLoans;
    }
    
    /**
     * Send overdue notification to borrower
     */
    private void sendOverdueNotification(Loan loan) {
        try {
            String subject = "Overdue Book Return Notice";
            String message = buildOverdueMessage(loan);
            
            emailNotificationService.sendNotification(
                loan.getBorrowerEmail(),
                loan.getBorrowerName(),
                subject,
                message
            );
            
            logger.info("Sent overdue notification for loan {} to {}", loan.getId(), loan.getBorrowerEmail());
        } catch (Exception e) {
            logger.error("Failed to send overdue notification for loan {}", loan.getId(), e);
        }
    }
    
    /**
     * Send due reminder notification to borrower
     */
    private void sendDueReminderNotification(Loan loan) {
        try {
            String subject = "Book Return Reminder";
            String message = buildDueReminderMessage(loan);
            
            emailNotificationService.sendNotification(
                loan.getBorrowerEmail(),
                loan.getBorrowerName(),
                subject,
                message
            );
            
            logger.info("Sent due reminder for loan {} to {}", loan.getId(), loan.getBorrowerEmail());
        } catch (Exception e) {
            logger.error("Failed to send due reminder for loan {}", loan.getId(), e);
        }
    }
    
    /**
     * Send loan confirmation notification
     */
    public void sendLoanConfirmation(Loan loan) {
        try {
            String subject = "Book Loan Confirmation";
            String message = buildLoanConfirmationMessage(loan);
            
            emailNotificationService.sendNotification(
                loan.getBorrowerEmail(),
                loan.getBorrowerName(),
                subject,
                message
            );
            
            loanTrackingService.recordNotificationSent(loan.getId(), "LOAN_CONFIRMATION");
            logger.info("Sent loan confirmation for loan {} to {}", loan.getId(), loan.getBorrowerEmail());
        } catch (Exception e) {
            logger.error("Failed to send loan confirmation for loan {}", loan.getId(), e);
        }
    }
    
    /**
     * Send return confirmation notification
     */
    public void sendReturnConfirmation(Loan loan) {
        try {
            String subject = "Book Return Confirmation";
            String message = buildReturnConfirmationMessage(loan);
            
            emailNotificationService.sendNotification(
                loan.getBorrowerEmail(),
                loan.getBorrowerName(),
                subject,
                message
            );
            
            loanTrackingService.recordNotificationSent(loan.getId(), "RETURN_CONFIRMATION");
            logger.info("Sent return confirmation for loan {} to {}", loan.getId(), loan.getBorrowerEmail());
        } catch (Exception e) {
            logger.error("Failed to send return confirmation for loan {}", loan.getId(), e);
        }
    }
    
    /**
     * Build overdue notification message
     */
    private String buildOverdueMessage(Loan loan) {
        long daysOverdue = loan.getDaysOverdue();
        return String.format(
            "Dear %s,\n\n" +
            "This is a notice that the following book is overdue:\n\n" +
            "Book: %s\n" +
            "Due Date: %s\n" +
            "Days Overdue: %d\n\n" +
            "Please return the book as soon as possible to avoid additional fees.\n\n" +
            "Thank you,\nLibrary Management System",
            loan.getBorrowerName(),
            loan.getBook().getTitle(),
            loan.getDueDate(),
            daysOverdue
        );
    }
    
    /**
     * Build due reminder message
     */
    private String buildDueReminderMessage(Loan loan) {
        long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), loan.getDueDate());
        return String.format(
            "Dear %s,\n\n" +
            "This is a friendly reminder that the following book is due soon:\n\n" +
            "Book: %s\n" +
            "Due Date: %s\n" +
            "Days Until Due: %d\n\n" +
            "Please return the book by the due date to avoid late fees.\n\n" +
            "Thank you,\nLibrary Management System",
            loan.getBorrowerName(),
            loan.getBook().getTitle(),
            loan.getDueDate(),
            daysUntilDue
        );
    }
    
    /**
     * Build loan confirmation message
     */
    private String buildLoanConfirmationMessage(Loan loan) {
        return String.format(
            "Dear %s,\n\n" +
            "Your book loan has been confirmed:\n\n" +
            "Book: %s\n" +
            "Loan Date: %s\n" +
            "Due Date: %s\n" +
            "Loan ID: %d\n\n" +
            "Please return the book by the due date.\n\n" +
            "Thank you,\nLibrary Management System",
            loan.getBorrowerName(),
            loan.getBook().getTitle(),
            loan.getLoanDate(),
            loan.getDueDate(),
            loan.getId()
        );
    }
    
    /**
     * Build return confirmation message
     */
    private String buildReturnConfirmationMessage(Loan loan) {
        String onTimeStatus = loan.getReturnDate().isAfter(loan.getDueDate()) ? "LATE" : "ON TIME";
        return String.format(
            "Dear %s,\n\n" +
            "Your book return has been confirmed:\n\n" +
            "Book: %s\n" +
            "Return Date: %s\n" +
            "Due Date: %s\n" +
            "Status: %s\n" +
            "Loan ID: %d\n\n" +
            "Thank you for using our library services.\n\n" +
            "Library Management System",
            loan.getBorrowerName(),
            loan.getBook().getTitle(),
            loan.getReturnDate(),
            loan.getDueDate(),
            onTimeStatus,
            loan.getId()
        );
    }
    
    /**
     * Manual trigger for overdue processing (for testing or admin use)
     */
    public void triggerOverdueProcessing() {
        processOverdueLoans();
    }
    
    /**
     * Manual trigger for due reminders (for testing or admin use)
     */
    public void triggerDueReminders() {
        sendDueReminders();
    }
}