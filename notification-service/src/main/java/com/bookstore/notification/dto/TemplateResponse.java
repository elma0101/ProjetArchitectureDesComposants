package com.bookstore.notification.dto;

import com.bookstore.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for notification template response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateResponse {

    private Long id;
    private String name;
    private NotificationType type;
    private String subject;
    private String content;
    private String description;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
