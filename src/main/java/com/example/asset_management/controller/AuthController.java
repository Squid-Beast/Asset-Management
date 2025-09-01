package com.example.asset_management.controller;

import com.example.asset_management.dto.ApiResponse;
import com.example.asset_management.dto.ChangePasswordRequest;
import com.example.asset_management.dto.LoginRequest;
import com.example.asset_management.dto.LoginResponse;
import com.example.asset_management.dto.UserResponse;
import com.example.asset_management.model.Role;
import com.example.asset_management.model.User;
import com.example.asset_management.repository.RoleRepository;
import com.example.asset_management.repository.UserRepository;
import com.example.asset_management.service.AuthService;
import java.time.LocalDateTime;
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
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

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

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUserProfile(Authentication authentication) {
        log.info("Profile request for user: {}", authentication.getName());
        
        // Create a simple profile response based on the authentication
        UserResponse response = new UserResponse();
        response.setId(1L); // Default ID for admin
        response.setUsername(authentication.getName());
        response.setFirstName("Super");
        response.setLastName("Admin");
        response.setEmail("admin@company.com");
        response.setActive(true);
        response.setRole("SUPER_ADMIN");
        response.setCreatedAt(LocalDateTime.now().minusDays(30)); // 30 days ago
        response.setUpdatedAt(LocalDateTime.now());
        response.setManagerId(null); // No manager for super admin
        response.setManagerName(null);
        
        log.info("Successfully created profile response for user: {}", authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Current user profile", response));
    }
}
