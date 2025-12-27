package com.bookstore.notification.repository;

import com.bookstore.notification.entity.NotificationTemplate;
import com.bookstore.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for NotificationTemplate entity
 */
@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    /**
     * Find template by name
     */
    Optional<NotificationTemplate> findByName(String name);

    /**
     * Find active templates by type
     */
    List<NotificationTemplate> findByTypeAndActiveTrue(NotificationType type);

    /**
     * Find all active templates
     */
    List<NotificationTemplate> findByActiveTrue();
}
