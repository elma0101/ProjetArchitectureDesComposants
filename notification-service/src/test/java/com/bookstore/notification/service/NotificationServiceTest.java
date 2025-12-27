package com.bookstore.notification.service;

import com.bookstore.notification.dto.NotificationRequest;
import com.bookstore.notification.dto.NotificationResponse;
import com.bookstore.notification.entity.Notification;
import com.bookstore.notification.entity.NotificationStatus;
import com.bookstore.notification.entity.NotificationType;
import com.bookstore.notification.repository.NotificationRepository;
import com.bookstore.notification.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationRequest notificationRequest;
    private Notification notification;

    @BeforeEach
    void setUp() {
        notificationRequest = NotificationRequest.builder()
                .userId(1L)
                .type(NotificationType.EMAIL)
                .recipient("test@example.com")
                .subject("Test Subject")
                .content("Test Content")
                .build();

        notification = Notification.builder()
                .id(1L)
                .userId(1L)
                .type(NotificationType.EMAIL)
                .recipient("test@example.com")
                .subject("Test Subject")
                .content("Test Content")
                .status(NotificationStatus.PENDING)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void sendNotification_Success() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        doNothing().when(emailService).sendSimpleEmail(anyString(), anyString(), anyString());

        // Act
        NotificationResponse response = notificationService.sendNotification(notificationRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals(NotificationType.EMAIL, response.getType());
        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(emailService, times(1)).sendSimpleEmail(anyString(), anyString(), anyString());
    }

    @Test
    void getUserNotifications_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> notificationPage = new PageImpl<>(Arrays.asList(notification));
        when(notificationRepository.findByUserId(1L, pageable)).thenReturn(notificationPage);

        // Act
        Page<NotificationResponse> result = notificationService.getUserNotifications(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(notificationRepository, times(1)).findByUserId(1L, pageable);
    }

    @Test
    void getNotification_Success() {
        // Arrange
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        // Act
        NotificationResponse response = notificationService.getNotification(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getUserId());
        verify(notificationRepository, times(1)).findById(1L);
    }

    @Test
    void getNotification_NotFound() {
        // Arrange
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> notificationService.getNotification(1L));
        verify(notificationRepository, times(1)).findById(1L);
    }
}
