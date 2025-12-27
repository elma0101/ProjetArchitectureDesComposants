package com.bookstore.notification.service;

import com.bookstore.notification.dto.TemplateRequest;
import com.bookstore.notification.dto.TemplateResponse;
import com.bookstore.notification.entity.NotificationTemplate;
import com.bookstore.notification.entity.NotificationType;
import com.bookstore.notification.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateServiceTest {

    @Mock
    private NotificationTemplateRepository templateRepository;

    @InjectMocks
    private NotificationTemplateService templateService;

    private TemplateRequest templateRequest;
    private NotificationTemplate template;

    @BeforeEach
    void setUp() {
        templateRequest = TemplateRequest.builder()
                .name("test_template")
                .type(NotificationType.EMAIL)
                .subject("Test Subject")
                .content("Test Content")
                .description("Test Description")
                .active(true)
                .build();

        template = NotificationTemplate.builder()
                .id(1L)
                .name("test_template")
                .type(NotificationType.EMAIL)
                .subject("Test Subject")
                .content("Test Content")
                .description("Test Description")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createTemplate_Success() {
        // Arrange
        when(templateRepository.save(any(NotificationTemplate.class))).thenReturn(template);

        // Act
        TemplateResponse response = templateService.createTemplate(templateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("test_template", response.getName());
        assertEquals(NotificationType.EMAIL, response.getType());
        verify(templateRepository, times(1)).save(any(NotificationTemplate.class));
    }

    @Test
    void updateTemplate_Success() {
        // Arrange
        when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(templateRepository.save(any(NotificationTemplate.class))).thenReturn(template);

        // Act
        TemplateResponse response = templateService.updateTemplate(1L, templateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("test_template", response.getName());
        verify(templateRepository, times(1)).findById(1L);
        verify(templateRepository, times(1)).save(any(NotificationTemplate.class));
    }

    @Test
    void getTemplate_Success() {
        // Arrange
        when(templateRepository.findById(1L)).thenReturn(Optional.of(template));

        // Act
        TemplateResponse response = templateService.getTemplate(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(templateRepository, times(1)).findById(1L);
    }

    @Test
    void getTemplateByName_Success() {
        // Arrange
        when(templateRepository.findByName("test_template")).thenReturn(Optional.of(template));

        // Act
        TemplateResponse response = templateService.getTemplateByName("test_template");

        // Assert
        assertNotNull(response);
        assertEquals("test_template", response.getName());
        verify(templateRepository, times(1)).findByName("test_template");
    }

    @Test
    void getAllTemplates_Success() {
        // Arrange
        when(templateRepository.findAll()).thenReturn(Arrays.asList(template));

        // Act
        List<TemplateResponse> templates = templateService.getAllTemplates();

        // Assert
        assertNotNull(templates);
        assertEquals(1, templates.size());
        verify(templateRepository, times(1)).findAll();
    }

    @Test
    void getActiveTemplatesByType_Success() {
        // Arrange
        when(templateRepository.findByTypeAndActiveTrue(NotificationType.EMAIL))
                .thenReturn(Arrays.asList(template));

        // Act
        List<TemplateResponse> templates = templateService.getActiveTemplatesByType(NotificationType.EMAIL);

        // Assert
        assertNotNull(templates);
        assertEquals(1, templates.size());
        verify(templateRepository, times(1)).findByTypeAndActiveTrue(NotificationType.EMAIL);
    }

    @Test
    void deleteTemplate_Success() {
        // Arrange
        doNothing().when(templateRepository).deleteById(1L);

        // Act
        templateService.deleteTemplate(1L);

        // Assert
        verify(templateRepository, times(1)).deleteById(1L);
    }
}
