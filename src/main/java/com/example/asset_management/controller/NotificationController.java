package com.example.asset_management.controller;

import com.example.asset_management.dto.ApiResponse;
import com.example.asset_management.model.Notification;
import com.example.asset_management.model.User;
import com.example.asset_management.repository.NotificationRepository;
import com.example.asset_management.repository.UserRepository;
import com.example.asset_management.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    @PostConstruct
    public void init() {
        log.info("NotificationController has been initialized!");
    }
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getUserNotifications(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", notifications));
    }
    
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<Notification>>> getUnreadNotifications(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
        
        return ResponseEntity.ok(ApiResponse.success("Unread notifications retrieved successfully", notifications));
    }
    
    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Long count = notificationRepository.countUnreadByUserId(user.getId());
        
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved successfully", count));
    }
    
    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        notificationService.markAsRead(id);
        
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", "Success"));
    }
    
    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<String>> markAllAsRead(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        notificationRepository.markAllAsReadByUserId(user.getId());
        
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", "Success"));
    }
}
