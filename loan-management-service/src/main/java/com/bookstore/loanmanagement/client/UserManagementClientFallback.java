package com.bookstore.loanmanagement.client;

import com.bookstore.loanmanagement.dto.UserResponse;
import com.bookstore.loanmanagement.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserManagementClientFallback implements UserManagementClient {

    @Override
    public UserResponse getUserById(Long id) {
        log.error("User Management Service is unavailable. Fallback triggered for getUserById: {}", id);
        throw new ServiceUnavailableException("User Management Service is currently unavailable");
    }
}
