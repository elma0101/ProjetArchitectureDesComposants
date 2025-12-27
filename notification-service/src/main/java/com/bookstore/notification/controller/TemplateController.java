package com.bookstore.notification.controller;

import com.bookstore.notification.dto.TemplateRequest;
import com.bookstore.notification.dto.TemplateResponse;
import com.bookstore.notification.entity.NotificationType;
import com.bookstore.notification.service.NotificationTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for notification template operations
 */
@RestController
@RequestMapping("/api/notifications/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final NotificationTemplateService templateService;

    /**
     * Create a new template
     */
    @PostMapping
    public ResponseEntity<TemplateResponse> createTemplate(@Valid @RequestBody TemplateRequest request) {
        TemplateResponse response = templateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing template
     */
    @PutMapping("/{id}")
    public ResponseEntity<TemplateResponse> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody TemplateRequest request) {
        TemplateResponse response = templateService.updateTemplate(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get template by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TemplateResponse> getTemplate(@PathVariable Long id) {
        TemplateResponse template = templateService.getTemplate(id);
        return ResponseEntity.ok(template);
    }

    /**
     * Get template by name
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<TemplateResponse> getTemplateByName(@PathVariable String name) {
        TemplateResponse template = templateService.getTemplateByName(name);
        return ResponseEntity.ok(template);
    }

    /**
     * Get all templates
     */
    @GetMapping
    public ResponseEntity<List<TemplateResponse>> getAllTemplates() {
        List<TemplateResponse> templates = templateService.getAllTemplates();
        return ResponseEntity.ok(templates);
    }

    /**
     * Get active templates by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<TemplateResponse>> getActiveTemplatesByType(@PathVariable NotificationType type) {
        List<TemplateResponse> templates = templateService.getActiveTemplatesByType(type);
        return ResponseEntity.ok(templates);
    }

    /**
     * Delete a template
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
