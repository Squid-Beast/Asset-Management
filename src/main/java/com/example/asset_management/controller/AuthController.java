package com.example.asset_management.controller;

import com.example.asset_management.dto.ApiResponse;
import com.example.asset_management.dto.ChangePasswordRequest;
import com.example.asset_management.dto.LoginRequest;
import com.example.asset_management.dto.LoginResponse;
import com.example.asset_management.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            log.error("Login failed for user: {}", loginRequest.getUsername(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid credentials"));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        try {
            authService.changePassword(authentication.getName(), request);
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
        } catch (Exception e) {
            log.error("Password change failed for user: {}", authentication.getName(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Current user", authentication.getName()));
    }
}
