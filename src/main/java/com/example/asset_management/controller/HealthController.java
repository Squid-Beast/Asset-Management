package com.example.asset_management.controller;

import com.example.asset_management.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("service", "Asset Management API");
        healthInfo.put("version", "1.0.0");
        
        return ResponseEntity.ok(ApiResponse.success("Service is healthy", healthInfo));
    }

    @GetMapping("/ready")
    public ResponseEntity<ApiResponse<String>> readiness() {
        // Add database connectivity check here if needed
        return ResponseEntity.ok(ApiResponse.success("Service is ready", null));
    }

    @GetMapping("/live")
    public ResponseEntity<ApiResponse<String>> liveness() {
        return ResponseEntity.ok(ApiResponse.success("Service is alive", null));
    }
}
