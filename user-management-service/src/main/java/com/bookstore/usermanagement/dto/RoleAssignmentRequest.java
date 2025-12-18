package com.bookstore.usermanagement.dto;

import com.bookstore.usermanagement.entity.Role;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public class RoleAssignmentRequest {
    
    @NotEmpty(message = "At least one role must be assigned")
    private Set<Role> roles;
    
    public RoleAssignmentRequest() {}
    
    public RoleAssignmentRequest(Set<Role> roles) {
        this.roles = roles;
    }
    
    public Set<Role> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
