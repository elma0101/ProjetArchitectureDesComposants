package com.bookstore.notification.service;

import com.bookstore.notification.dto.NotificationRequest;
import com.bookstore.notification.dto.NotificationResponse;
import com.bookstore.notification.entity.Notification;
import com.bookstore.notification.entity.NotificationStatus;
import com.bookstore.notification.entity.NotificationTemplate;
import com.bookstore.notification.entity.NotificationType;
import com.bookstore.notification.repository.NotificationRepository;
import com.bookstore.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final EmailService emailService;

    /**
     * Send a notification
     */
    @Transactional
    public NotificationResponse sendNotification(NotificationRequest request) {
        log.info("Sending notification to user: {}", request.getUserId());

        // Create notification record
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .content(request.getContent())
                .status(NotificationStatus.PENDING)
                .templateId(request.getTemplateId())
                .retryCount(0)
                .build();

        notification = notificationRepository.save(notification);

        // Send notification based on type
        try {
            if (request.getType() == NotificationType.EMAIL) {
                if (request.getTemplateId() != null && request.getTemplateVariables() != null) {
                    // Send using template
                    NotificationTemplate template = templateRepository.findById(request.getTemplateId())
                            .orElseThrow(() -> new RuntimeException("Template not found"));
                    
                    String processedContent = processTemplate(template.getContent(), request.getTemplateVariables());
                    emailService.sendSimpleEmail(request.getRecipient(), request.getSubject(), processedContent);
                } else {
                    // Send simple email
                    emailService.sendSimpleEmail(request.getRecipient(), request.getSubject(), request.getContent());
                }
            }

            // Update notification status
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);

            log.info("Notification sent successfully: {}", notification.getId());
        } catch (Exception e) {
            log.error("Failed to send notification: {}", notification.getId(), e);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            notification = notificationRepository.save(notification);
        }

        return mapToResponse(notification);
    }

    /**
     * Get notifications for a user
     */
    public Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get notification by ID
     */
    public NotificationResponse getNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        return mapToResponse(notification);
    }

    /**
     * Retry failed notifications
     */
    @Transactional
    public void retryFailedNotifications(int maxRetries) {
        log.info("Retrying failed notifications with max retries: {}", maxRetries);
        
        var failedNotifications = notificationRepository
                .findByStatusAndRetryCountLessThan(NotificationStatus.FAILED, maxRetries);

        for (Notification notification : failedNotifications) {
            try {
                notification.setStatus(NotificationStatus.RETRYING);
                notification.setRetryCount(notification.getRetryCount() + 1);
                notificationRepository.save(notification);

                // Retry sending
                if (notification.getType() == NotificationType.EMAIL) {
                    emailService.sendSimpleEmail(
                            notification.getRecipient(),
                            notification.getSubject(),
                            notification.getContent()
                    );
                }

                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                notificationRepository.save(notification);

                log.info("Notification retry successful: {}", notification.getId());
            } catch (Exception e) {
                log.error("Notification retry failed: {}", notification.getId(), e);
                notification.setStatus(NotificationStatus.FAILED);
                notification.setErrorMessage(e.getMessage());
                notificationRepository.save(notification);
            }
        }
    }

    /**
     * Process template with variables
     */
    private String processTemplate(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    /**
     * Map entity to response DTO
     */
    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .recipient(notification.getRecipient())
                .subject(notification.getSubject())
                .status(notification.getStatus())
                .errorMessage(notification.getErrorMessage())
                .retryCount(notification.getRetryCount())
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .build();
    }
}
