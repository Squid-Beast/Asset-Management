package com.example.asset_management.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private Long roleId;
    private Long departmentId;
    private Long managerId;
}
