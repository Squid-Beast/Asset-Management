package com.example.asset_management.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String passwordHash;
    private Long roleId;
    private Long departmentId;
    private Long managerId;
    private Boolean isActive;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Relationships (not stored in database, used for joins)
    private Role role;
    private Department department;
    private User manager;
}
