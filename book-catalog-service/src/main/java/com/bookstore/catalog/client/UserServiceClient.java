package com.bookstore.catalog.client;

import com.bookstore.catalog.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-management-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    UserResponse getUserById(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);

    @GetMapping("/api/users/{id}/exists")
    Boolean userExists(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);
}
