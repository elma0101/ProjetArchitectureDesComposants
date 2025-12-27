package com.bookstore.notification.dto;

import com.bookstore.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating/updating notification templates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateRequest {

    @NotBlank(message = "Template name is required")
    private String name;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Content is required")
    private String content;

    private String description;

    private Boolean active;
}
