package com.example.asset_management.controller;

import com.example.asset_management.dto.ApiResponse;
import com.example.asset_management.dto.CreateAssetRequest;
import com.example.asset_management.dto.CreateUserRequest;
import com.example.asset_management.model.Asset;
import com.example.asset_management.model.AssetCategory;
import com.example.asset_management.model.Department;
import com.example.asset_management.model.Role;
import com.example.asset_management.model.User;
import com.example.asset_management.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AdminController {

    private final AdminService adminService;

    // Asset Management APIs
    @PostMapping("/assets")
    public ResponseEntity<ApiResponse<Asset>> createAsset(
            @RequestBody CreateAssetRequest request,
            Authentication authentication) {
        try {
            Asset createdAsset = adminService.createAsset(request, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Asset created successfully", createdAsset));
        } catch (Exception e) {
            log.error("Failed to create asset", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/assets/bulk-import")
    public ResponseEntity<ApiResponse<Map<String, Object>>> bulkImportAssets(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            Map<String, Object> result = adminService.bulkImportAssets(file, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Assets imported successfully", result));
        } catch (Exception e) {
            log.error("Failed to import assets", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/assets")
    public ResponseEntity<ApiResponse<List<Asset>>> getAllAssets(Authentication authentication) {
        try {
            List<Asset> assets = adminService.getAllAssets(authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Assets retrieved successfully", assets));
        } catch (Exception e) {
            log.error("Failed to get assets", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/assets/{id}")
    public ResponseEntity<ApiResponse<Asset>> getAssetById(@PathVariable Long id, Authentication authentication) {
        try {
            Asset asset = adminService.getAssetById(id, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Asset retrieved successfully", asset));
        } catch (Exception e) {
            log.error("Failed to get asset", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/assets/{id}")
    public ResponseEntity<ApiResponse<Asset>> updateAsset(
            @PathVariable Long id,
            @RequestBody CreateAssetRequest request,
            Authentication authentication) {
        try {
            Asset updatedAsset = adminService.updateAsset(id, request, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Asset updated successfully", updatedAsset));
        } catch (Exception e) {
            log.error("Failed to update asset", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/assets/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAsset(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            adminService.deleteAsset(id, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Asset deleted successfully", "Success"));
        } catch (Exception e) {
            log.error("Failed to delete asset", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/asset-categories")
    public ResponseEntity<ApiResponse<List<AssetCategory>>> getAssetCategories() {
        try {
            List<AssetCategory> categories = adminService.getAllAssetCategories();
            return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
        } catch (Exception e) {
            log.error("Failed to get asset categories", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/asset-categories")
    public ResponseEntity<ApiResponse<AssetCategory>> createAssetCategory(
            @RequestBody AssetCategory category,
            Authentication authentication) {
        try {
            AssetCategory createdCategory = adminService.createAssetCategory(category, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Category created successfully", createdCategory));
        } catch (Exception e) {
            log.error("Failed to create asset category", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/asset-categories/{id}")
    public ResponseEntity<ApiResponse<AssetCategory>> updateAssetCategory(
            @PathVariable Long id,
            @RequestBody AssetCategory category,
            Authentication authentication) {
        try {
            AssetCategory updatedCategory = adminService.updateAssetCategory(id, category, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Category updated successfully", updatedCategory));
        } catch (Exception e) {
            log.error("Failed to update asset category", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/asset-categories/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAssetCategory(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            adminService.deleteAssetCategory(id, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", "Success"));
        } catch (Exception e) {
            log.error("Failed to delete asset category", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // User Management APIs
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers(Authentication authentication) {
        try {
            List<User> users = adminService.getAllUsers(authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
        } catch (Exception e) {
            log.error("Failed to get users", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<User>> createUser(
            @RequestBody CreateUserRequest request,
            Authentication authentication) {
        try {
            User createdUser = adminService.createUser(request, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("User created successfully", createdUser));
        } catch (Exception e) {
            log.error("Failed to create user", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<User>> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String role = request.get("role");
            String department = request.get("department");
            User updatedUser = adminService.updateUserRole(id, role, department, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("User role updated successfully", updatedUser));
        } catch (Exception e) {
            log.error("Failed to update user role", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<User>> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request,
            Authentication authentication) {
        try {
            Boolean active = request.get("active");
            User updatedUser = adminService.updateUserStatus(id, active, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("User status updated successfully", updatedUser));
        } catch (Exception e) {
            log.error("Failed to update user status", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id, Authentication authentication) {
        try {
            User user = adminService.getUserById(id, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
        } catch (Exception e) {
            log.error("Failed to get user", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable Long id,
            @RequestBody CreateUserRequest request,
            Authentication authentication) {
        try {
            User updatedUser = adminService.updateUser(id, request, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUser));
        } catch (Exception e) {
            log.error("Failed to update user", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            adminService.deleteUser(id, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully", "Success"));
        } catch (Exception e) {
            log.error("Failed to delete user", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // System Configuration APIs
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemConfig(Authentication authentication) {
        try {
            Map<String, Object> config = adminService.getSystemConfig(authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("System config retrieved successfully", config));
        } catch (Exception e) {
            log.error("Failed to get system config", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateSystemConfig(
            @RequestBody Map<String, Object> config,
            Authentication authentication) {
        try {
            Map<String, Object> updatedConfig = adminService.updateSystemConfig(config, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("System config updated successfully", updatedConfig));
        } catch (Exception e) {
            log.error("Failed to update system config", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Reports APIs
    @GetMapping("/reports/asset-utilization")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAssetUtilizationReport(Authentication authentication) {
        try {
            Map<String, Object> report = adminService.getAssetUtilizationReport(authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Asset utilization report generated", report));
        } catch (Exception e) {
            log.error("Failed to generate asset utilization report", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/reports/user-activity")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserActivityReport(Authentication authentication) {
        try {
            Map<String, Object> report = adminService.getUserActivityReport(authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("User activity report generated", report));
        } catch (Exception e) {
            log.error("Failed to generate user activity report", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/reports/overdue-assets")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverdueAssetsReport(Authentication authentication) {
        try {
            Map<String, Object> report = adminService.getOverdueAssetsReport(authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Overdue assets report generated", report));
        } catch (Exception e) {
            log.error("Failed to generate overdue assets report", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/reports/department-summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDepartmentSummaryReport(Authentication authentication) {
        try {
            Map<String, Object> report = adminService.getDepartmentSummaryReport(authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Department summary report generated", report));
        } catch (Exception e) {
            log.error("Failed to generate department summary report", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Department and Role APIs
    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<Department>>> getDepartments() {
        try {
            List<Department> departments = adminService.getAllDepartments();
            return ResponseEntity.ok(ApiResponse.success("Departments retrieved successfully", departments));
        } catch (Exception e) {
            log.error("Failed to get departments", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<Role>>> getRoles() {
        try {
            List<Role> roles = adminService.getAllRoles();
            return ResponseEntity.ok(ApiResponse.success("Roles retrieved successfully", roles));
        } catch (Exception e) {
            log.error("Failed to get roles", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Data Export APIs
    @PostMapping("/export")
    public ResponseEntity<ApiResponse<Map<String, Object>>> exportData(
            @RequestBody Map<String, Object> exportRequest,
            Authentication authentication) {
        try {
            Map<String, Object> result = adminService.exportData(exportRequest, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Data exported successfully", result));
        } catch (Exception e) {
            log.error("Failed to export data", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
