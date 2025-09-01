package com.example.asset_management.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class UpdateUserRequest {
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    private String firstName;
    private String lastName;
    
    @Email(message = "Email must be valid")
    private String email;
    
    private String role;
    private Long managerId;
    private Boolean active;
}
