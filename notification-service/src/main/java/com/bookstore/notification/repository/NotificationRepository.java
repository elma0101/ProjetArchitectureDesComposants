package com.bookstore.notification.repository;

import com.bookstore.notification.entity.Notification;
import com.bookstore.notification.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Notification entity
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find notifications by user ID
     */
    Page<Notification> findByUserId(Long userId, Pageable pageable);

    /**
     * Find notifications by status
     */
    List<Notification> findByStatus(NotificationStatus status);

    /**
     * Find notifications by status and retry count less than max
     */
    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, Integer maxRetries);

    /**
     * Find notifications created after a specific date
     */
    List<Notification> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Count notifications by user ID and status
     */
    long countByUserIdAndStatus(Long userId, NotificationStatus status);
}
