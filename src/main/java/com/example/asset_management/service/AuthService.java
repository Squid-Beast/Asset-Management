package com.example.asset_management.service;

import com.example.asset_management.dto.ChangePasswordRequest;
import com.example.asset_management.dto.LoginRequest;
import com.example.asset_management.dto.LoginResponse;
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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
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
}
