package com.example.asset_management.controller;

import com.example.asset_management.dto.ApiResponse;
import com.example.asset_management.dto.UserResponse;
import com.example.asset_management.dto.CreateUserRequest;
import com.example.asset_management.dto.UpdateUserRequest;
import com.example.asset_management.model.User;
import com.example.asset_management.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(Authentication authentication) {
        try {
            List<UserResponse> users = userService.getAllUsers();
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            log.error("Failed to get all users", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to retrieve users"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        try {
            UserResponse user = userService.getUserById(id);
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (Exception e) {
            log.error("Failed to get user by ID: {}", id, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication) {
        try {
            UserResponse createdUser = userService.createUser(request);
            return ResponseEntity.ok(ApiResponse.success("User created successfully", createdUser));
        } catch (Exception e) {
            log.error("Failed to create user", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        try {
            UserResponse updatedUser = userService.updateUser(id, request);
            return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUser));
        } catch (Exception e) {
            log.error("Failed to update user: {}", id, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
        } catch (Exception e) {
            log.error("Failed to delete user: {}", id, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request,
            Authentication authentication) {
        try {
            Boolean active = request.get("active");
            if (active == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Active status is required"));
            }
            UserResponse updatedUser = userService.updateUserStatus(id, active);
            return ResponseEntity.ok(ApiResponse.success("User status updated successfully", updatedUser));
        } catch (Exception e) {
            log.error("Failed to update user status: {} -> {}", id, request, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(Authentication authentication) {
        try {
            UserResponse user = userService.getUserProfile(authentication.getName());
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (Exception e) {
            log.error("Failed to get user profile", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStatistics(Authentication authentication) {
        try {
            Map<String, Object> statistics = userService.getUserStatistics(authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("User statistics retrieved successfully", statistics));
        } catch (Exception e) {
            log.error("Failed to get user statistics", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
