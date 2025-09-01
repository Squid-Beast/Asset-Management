package com.example.asset_management.service;

import com.example.asset_management.dto.ChangePasswordRequest;
import com.example.asset_management.dto.LoginRequest;
import com.example.asset_management.dto.LoginResponse;
import com.example.asset_management.dto.UserResponse;
import com.example.asset_management.model.Role;
import com.example.asset_management.model.User;
import com.example.asset_management.repository.RoleRepository;
import com.example.asset_management.repository.UserRepository;
import com.example.asset_management.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final KafkaEventService kafkaEventService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findActiveByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get the role name from roleId
        String roleName = "USER"; // Default role
        if (user.getRoleId() != null) {
            try {
                Role role = roleRepository.findById(user.getRoleId()).orElse(null);
                if (role != null) {
                    roleName = role.getName();
                }
            } catch (Exception e) {
                log.warn("Could not fetch role for user: {}", loginRequest.getUsername(), e);
            }
        }

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Publish user activity event
        Map<String, Object> activityData = new HashMap<>();
        activityData.put("username", user.getUsername());
        activityData.put("loginTime", LocalDateTime.now());
        activityData.put("userAgent", "asset-management-api");
        kafkaEventService.publishUserActivityEvent("UserLogin", user.getId(), activityData);

        LoginResponse response = new LoginResponse();
        response.setToken(jwt);
        response.setUsername(user.getUsername());
        response.setRole(roleName);
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setUserId(user.getId());
        return response;
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findActiveByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Verify new password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirmation do not match");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", username);
    }

    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return userRepository.findActiveByUsername(authentication.getName());
        }
        return Optional.empty();
    }

    public boolean hasRole(String roleName) {
        return getCurrentUser()
                .map(user -> {
                    if (user.getRoleId() != null) {
                        try {
                            Role role = roleRepository.findById(user.getRoleId()).orElse(null);
                            return role != null && role.getName().equals(roleName);
                        } catch (Exception e) {
                            log.warn("Could not fetch role for user: {}", user.getUsername(), e);
                        }
                    }
                    return false;
                })
                .orElse(false);
    }

    public boolean isManager() {
        return hasRole("MANAGER") || hasRole("SUPER_ADMIN");
    }

    public boolean isSuperAdmin() {
        return hasRole("SUPER_ADMIN");
    }

    public UserResponse getCurrentUserProfile(String username) {
        try {
            log.info("Getting user profile for username: {}", username);
            
            User user = userRepository.findActiveByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            log.info("Found user: {} {} (ID: {})", user.getFirstName(), user.getLastName(), user.getId());

            UserResponse response = new UserResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setEmail(user.getEmail());
            response.setActive(user.getIsActive() != null ? user.getIsActive() : false);
            response.setCreatedAt(user.getCreatedAt());
            response.setUpdatedAt(user.getUpdatedAt());
            response.setManagerId(user.getManagerId());
            
            // Get role name
            try {
                if (user.getRoleId() != null) {
                    Role role = roleRepository.findById(user.getRoleId()).orElse(null);
                    response.setRole(role != null ? role.getName() : "EMPLOYEE");
                } else {
                    response.setRole("EMPLOYEE");
                }
            } catch (Exception e) {
                log.warn("Could not fetch role for user: {}, setting default role", username, e);
                response.setRole("EMPLOYEE");
            }
            
            // Get manager name if managerId exists
            try {
                if (user.getManagerId() != null) {
                    userRepository.findById(user.getManagerId())
                            .ifPresent(manager -> response.setManagerName(
                                    manager.getFirstName() + " " + manager.getLastName()));
                }
            } catch (Exception e) {
                log.warn("Could not fetch manager for user: {}", username, e);
            }
            
            log.info("Successfully created user profile response for: {}", username);
            return response;
        } catch (Exception e) {
            log.error("Error getting user profile for username: {}", username, e);
            throw new RuntimeException("Failed to get user profile: " + e.getMessage(), e);
        }
    }
}
