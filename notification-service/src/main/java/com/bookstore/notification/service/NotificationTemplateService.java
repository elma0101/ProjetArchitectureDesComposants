package com.bookstore.notification.service;

import com.bookstore.notification.dto.TemplateRequest;
import com.bookstore.notification.dto.TemplateResponse;
import com.bookstore.notification.entity.NotificationTemplate;
import com.bookstore.notification.entity.NotificationType;
import com.bookstore.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing notification templates
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationTemplateService {

    private final NotificationTemplateRepository templateRepository;

    /**
     * Create a new template
     */
    @Transactional
    public TemplateResponse createTemplate(TemplateRequest request) {
        log.info("Creating notification template: {}", request.getName());

        NotificationTemplate template = NotificationTemplate.builder()
                .name(request.getName())
                .type(request.getType())
                .subject(request.getSubject())
                .content(request.getContent())
                .description(request.getDescription())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        template = templateRepository.save(template);
        log.info("Template created successfully: {}", template.getId());

        return mapToResponse(template);
    }

    /**
     * Update an existing template
     */
    @Transactional
    public TemplateResponse updateTemplate(Long id, TemplateRequest request) {
        log.info("Updating notification template: {}", id);

        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        template.setName(request.getName());
        template.setType(request.getType());
        template.setSubject(request.getSubject());
        template.setContent(request.getContent());
        template.setDescription(request.getDescription());
        if (request.getActive() != null) {
            template.setActive(request.getActive());
        }

        template = templateRepository.save(template);
        log.info("Template updated successfully: {}", template.getId());

        return mapToResponse(template);
    }

    /**
     * Get template by ID
     */
    public TemplateResponse getTemplate(Long id) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        return mapToResponse(template);
    }

    /**
     * Get template by name
     */
    public TemplateResponse getTemplateByName(String name) {
        NotificationTemplate template = templateRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        return mapToResponse(template);
    }

    /**
     * Get all templates
     */
    public List<TemplateResponse> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get active templates by type
     */
    public List<TemplateResponse> getActiveTemplatesByType(NotificationType type) {
        return templateRepository.findByTypeAndActiveTrue(type).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Delete a template
     */
    @Transactional
    public void deleteTemplate(Long id) {
        log.info("Deleting notification template: {}", id);
        templateRepository.deleteById(id);
    }

    /**
     * Map entity to response DTO
     */
    private TemplateResponse mapToResponse(NotificationTemplate template) {
        return TemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .type(template.getType())
                .subject(template.getSubject())
                .content(template.getContent())
                .description(template.getDescription())
                .active(template.getActive())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
