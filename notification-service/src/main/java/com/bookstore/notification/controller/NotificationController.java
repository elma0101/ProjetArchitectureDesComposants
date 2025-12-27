package com.bookstore.notification.controller;

import com.bookstore.notification.dto.NotificationRequest;
import com.bookstore.notification.dto.NotificationResponse;
import com.bookstore.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for notification operations
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Send a notification
     */
    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> sendNotification(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get notifications for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<NotificationResponse> notifications = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get notification by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable Long id) {
        NotificationResponse notification = notificationService.getNotification(id);
        return ResponseEntity.ok(notification);
    }

    /**
     * Retry failed notifications
     */
    @PostMapping("/retry")
    public ResponseEntity<Void> retryFailedNotifications(@RequestParam(defaultValue = "3") int maxRetries) {
        notificationService.retryFailedNotifications(maxRetries);
        return ResponseEntity.ok().build();
    }
}
