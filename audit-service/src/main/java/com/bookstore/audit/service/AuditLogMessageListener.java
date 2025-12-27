package com.bookstore.audit.service;

import com.bookstore.audit.config.RabbitMQConfig;
import com.bookstore.audit.dto.AuditLogRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogMessageListener {

    private final AuditLogService auditLogService;

    @RabbitListener(queues = RabbitMQConfig.AUDIT_QUEUE)
    public void handleAuditLogMessage(AuditLogRequest auditLogRequest) {
        try {
            log.debug("Received audit log message from queue: {}", auditLogRequest);
            auditLogService.createAuditLog(auditLogRequest);
            log.info("Successfully processed audit log message for action: {} on resource: {}",
                    auditLogRequest.getAction(), auditLogRequest.getResourceType());
        } catch (Exception e) {
            log.error("Error processing audit log message: {}", auditLogRequest, e);
            // In production, you might want to send this to a dead letter queue
            throw e;
        }
    }
}
