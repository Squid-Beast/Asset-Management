package com.example.asset_management.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String token;
    private String username;
    private String role;
    private String firstName;
    private String lastName;
    private Long userId;
}