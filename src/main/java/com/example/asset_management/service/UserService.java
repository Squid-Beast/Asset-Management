package com.example.asset_management.service;

import com.example.asset_management.dto.CreateUserRequest;
import com.example.asset_management.dto.UpdateUserRequest;
import com.example.asset_management.dto.UserResponse;
import com.example.asset_management.model.AssetLoan;
import com.example.asset_management.model.AssetLoan.LoanStatus;
import com.example.asset_management.model.Department;
import com.example.asset_management.model.Role;
import com.example.asset_management.model.User;
import com.example.asset_management.repository.RoleRepository;
import com.example.asset_management.repository.AssetLoanRepository;
import com.example.asset_management.repository.DepartmentRepository;
import com.example.asset_management.repository.RoleRepository;
import com.example.asset_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AssetLoanRepository assetLoanRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        // Use the roleId directly from the request
        user.setRoleId(request.getRoleId());
        user.setIsActive(true);
        user.setManagerId(request.getManagerId());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("User created: {}", savedUser.getUsername());
        
        return mapToUserResponse(savedUser);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new RuntimeException("Username already exists");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getRole() != null) {
            user.setRoleId(getRoleIdFromRole(request.getRole()));
        }

        if (request.getManagerId() != null) {
            user.setManagerId(request.getManagerId());
        }

        if (request.getActive() != null) {
            user.setIsActive(request.getActive());
        }

        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("User updated: {}", savedUser.getUsername());
        
        return mapToUserResponse(savedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user has active loans
        // This would require checking AssetLoanRepository
        // For now, we'll just deactivate the user instead of deleting
        
        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("User deactivated: {}", user.getUsername());
    }

    @Transactional
    public UserResponse updateUserStatus(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsActive(active);
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("User status updated: {} -> {}", savedUser.getUsername(), active);
        
        return mapToUserResponse(savedUser);
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setRole(getRoleFromRoleId(user.getRoleId()));
        response.setActive(user.getIsActive() != null ? user.getIsActive() : false);
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setManagerId(user.getManagerId());
        
        // Get manager name if managerId exists
        if (user.getManagerId() != null) {
            userRepository.findById(user.getManagerId())
                    .ifPresent(manager -> response.setManagerName(
                            manager.getFirstName() + " " + manager.getLastName()));
        }
        
        return response;
    }

    private Long getRoleIdFromRole(String role) {
        // Find role by name dynamically from database
        Role roleEntity = roleRepository.findByName(role)
                .orElseThrow(() -> new RuntimeException("Role not found: " + role));
        return roleEntity.getId();
    }

    private String getRoleFromRoleId(Long roleId) {
        if (roleId == null) return "Unknown";
        // Find role by ID dynamically from database
        Role role = roleRepository.findById(roleId)
                .orElse(null);
        return role != null ? role.getName() : "Unknown";
    }

    public UserResponse getUserProfile(String username) {
        User user = userRepository.findActiveByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return mapToUserResponse(user);
    }

    public Map<String, Object> getUserStatistics(String username) {
        User user = userRepository.findActiveByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Map<String, Object> statistics = new HashMap<>();
        
        // Get user's loan statistics
        List<AssetLoan> userLoans = assetLoanRepository.findByUserId(user.getId());
        
        statistics.put("totalLoans", userLoans.size());
        statistics.put("pendingLoans", userLoans.stream()
                .filter(loan -> loan.getStatus() == LoanStatus.pending_approval)
                .count());
        statistics.put("activeLoans", userLoans.stream()
                .filter(loan -> loan.getStatus() == LoanStatus.loaned)
                .count());
        statistics.put("overdueLoans", userLoans.stream()
                .filter(loan -> loan.getStatus() == LoanStatus.overdue)
                .count());
        
        // Get user's role and department info
        Role role = roleRepository.findById(user.getRoleId()).orElse(null);
        Department department = departmentRepository.findById(user.getDepartmentId()).orElse(null);
        
        statistics.put("role", role != null ? role.getName() : "Unknown");
        statistics.put("department", department != null ? department.getName() : "Unknown");
        
        // Get manager info if user has one
        if (user.getManagerId() != null) {
            User manager = userRepository.findById(user.getManagerId()).orElse(null);
            if (manager != null) {
                statistics.put("manager", manager.getFirstName() + " " + manager.getLastName());
            }
        }
        
        // Get subordinates if user is a manager
        if (role != null && "MANAGER".equals(role.getName())) {
            List<User> subordinates = userRepository.findSubordinatesByManagerId(user.getId());
            statistics.put("subordinatesCount", subordinates.size());
        }
        
        return statistics;
    }
}
