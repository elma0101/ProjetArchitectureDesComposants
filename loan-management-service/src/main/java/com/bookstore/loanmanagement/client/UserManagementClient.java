package com.bookstore.loanmanagement.client;

import com.bookstore.loanmanagement.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "user-management-service",
    fallback = UserManagementClientFallback.class
)
public interface UserManagementClient {

    @GetMapping("/api/users/{id}")
    UserResponse getUserById(@PathVariable("id") Long id);
}
