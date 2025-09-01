package com.example.asset_management.controller;

import com.example.asset_management.dto.ApiResponse;
import com.example.asset_management.repository.AssetCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class HealthController {

    private final AssetCategoryRepository assetCategoryRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        try {
            // Test database connection by fetching asset categories
            List<com.example.asset_management.model.AssetCategory> categories = assetCategoryRepository.findAll();
            
            Map<String, Object> healthData = Map.of(
                "status", "healthy",
                "database", "connected",
                "assetCategoriesCount", categories.size(),
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(ApiResponse.success("System is healthy", healthData));
        } catch (Exception e) {
            log.error("Health check failed", e);
            Map<String, Object> healthData = Map.of(
                "status", "unhealthy",
                "database", "disconnected",
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.internalServerError().body(ApiResponse.error("System is unhealthy"));
        }
    }

    @GetMapping("/test-db")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testDatabase() {
        try {
            // Test database connection by fetching asset categories
            List<com.example.asset_management.model.AssetCategory> categories = assetCategoryRepository.findAll();
            
            Map<String, Object> dbData = Map.of(
                "status", "connected",
                "categoriesCount", categories.size(),
                "categories", categories.stream().map(cat -> Map.of(
                    "id", cat.getId(),
                    "name", cat.getName(),
                    "description", cat.getDescription()
                )).toList()
            );
            
            return ResponseEntity.ok(ApiResponse.success("Database connection successful", dbData));
        } catch (Exception e) {
            log.error("Database test failed", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("Database connection failed: " + e.getMessage()));
        }
    }
}
