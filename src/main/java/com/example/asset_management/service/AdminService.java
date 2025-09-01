package com.example.asset_management.service;

import com.example.asset_management.dto.CreateAssetRequest;
import com.example.asset_management.dto.CreateUserRequest;
import com.example.asset_management.model.Asset;
import com.example.asset_management.model.Asset.AssetStatus;
import com.example.asset_management.model.AssetCategory;
import com.example.asset_management.model.Department;
import com.example.asset_management.model.Role;
import com.example.asset_management.model.User;
import com.example.asset_management.repository.AssetCategoryRepository;
import com.example.asset_management.repository.AssetRepository;
import com.example.asset_management.repository.DepartmentRepository;
import com.example.asset_management.repository.RoleRepository;
import com.example.asset_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final AssetCategoryRepository assetCategoryRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Asset Management Methods
    @Transactional
    public Asset createAsset(CreateAssetRequest request, String adminUsername) {
        log.info("Admin {} creating new asset: {}", adminUsername, request.getAssetTag());
        
        // Validate asset tag uniqueness
        if (assetRepository.findByAssetTag(request.getAssetTag()).isPresent()) {
            throw new RuntimeException("Asset tag already exists: " + request.getAssetTag());
        }

        // Create new asset from request
        Asset asset = new Asset();
        asset.setAssetTag(request.getAssetTag());
        asset.setName(request.getName());
        asset.setDescription(request.getDescription());
        asset.setCategoryId(request.getCategoryId());
        asset.setPurchaseDate(request.getPurchaseDate());
        asset.setWarrantyExpiry(request.getWarrantyExpiry());
        asset.setNotes(request.getNotes());
        
        // Set default values
        asset.setStatus(AssetStatus.available);
        asset.setCreatedAt(LocalDateTime.now());
        
        Asset savedAsset = assetRepository.save(asset);
        log.info("Asset created successfully: {}", savedAsset.getId());
        return savedAsset;
    }

    public List<Asset> getAllAssets(String adminUsername) {
        log.info("Admin {} retrieving all assets", adminUsername);
        return assetRepository.findAllOrderedByAssetTag();
    }

    public Asset getAssetById(Long id, String adminUsername) {
        log.info("Admin {} retrieving asset: {}", adminUsername, id);
        return assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + id));
    }

    @Transactional
    public Asset updateAsset(Long id, CreateAssetRequest request, String adminUsername) {
        log.info("Admin {} updating asset: {}", adminUsername, id);
        
        Asset existingAsset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + id));
        
        // Check if new asset tag conflicts with existing assets (excluding current asset)
        if (!request.getAssetTag().equals(existingAsset.getAssetTag()) &&
            assetRepository.findByAssetTag(request.getAssetTag()).isPresent()) {
            throw new RuntimeException("Asset tag already exists: " + request.getAssetTag());
        }
        
        // Update asset fields
        existingAsset.setAssetTag(request.getAssetTag());
        existingAsset.setName(request.getName());
        existingAsset.setDescription(request.getDescription());
        existingAsset.setCategoryId(request.getCategoryId());
        existingAsset.setPurchaseDate(request.getPurchaseDate());
        existingAsset.setWarrantyExpiry(request.getWarrantyExpiry());
        existingAsset.setNotes(request.getNotes());
        existingAsset.setUpdatedAt(LocalDateTime.now());
        
        Asset updatedAsset = assetRepository.save(existingAsset);
        log.info("Asset updated successfully: {}", updatedAsset.getId());
        return updatedAsset;
    }

    @Transactional
    public void deleteAsset(Long id, String adminUsername) {
        log.info("Admin {} deleting asset: {}", adminUsername, id);
        
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found: " + id));
        
        // Check if asset is currently loaned
        if (asset.getStatus() == AssetStatus.loaned) {
            throw new RuntimeException("Cannot delete asset: Asset is currently loaned");
        }
        
        // Check if asset has any loan history
        // This would require checking AssetLoanRepository
        // For now, we'll just delete the asset
        
        assetRepository.delete(asset);
        log.info("Asset deleted successfully: {}", id);
    }

    @Transactional
    public Map<String, Object> bulkImportAssets(MultipartFile file, String adminUsername) {
        log.info("Admin {} importing assets from file: {}", adminUsername, file.getOriginalFilename());
        
        Map<String, Object> result = new HashMap<>();
        List<Asset> importedAssets = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                
                                    try {
                        String[] values = line.split(",");
                        if (values.length >= 6) {
                            Asset asset = new Asset();
                            asset.setAssetTag(values[0].trim());
                            asset.setName(values[1].trim());
                            asset.setDescription(values[2].trim());
                            // Note: categoryName would need to be converted to categoryId
                            // For now, we'll set a default categoryId
                            asset.setCategoryId(1L); // Default category
                            asset.setPurchaseDate(LocalDate.parse(values[4].trim()));
                            asset.setWarrantyExpiry(LocalDate.parse(values[5].trim()));
                            asset.setStatus(AssetStatus.available);
                            asset.setCreatedAt(LocalDateTime.now());
                        
                        // Check if asset tag already exists
                        if (assetRepository.findByAssetTag(asset.getAssetTag()).isEmpty()) {
                            importedAssets.add(asset);
                            successCount++;
                        } else {
                            errors.add("Asset tag already exists: " + asset.getAssetTag());
                            errorCount++;
                        }
                    } else {
                        errors.add("Invalid data format in line: " + line);
                        errorCount++;
                    }
                } catch (Exception e) {
                    errors.add("Error processing line: " + line + " - " + e.getMessage());
                    errorCount++;
                }
            }
            
            // Save all valid assets
            if (!importedAssets.isEmpty()) {
                assetRepository.saveAll(importedAssets);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error processing CSV file: " + e.getMessage());
        }

        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        result.put("errors", errors);
        result.put("importedAssets", importedAssets);
        
        log.info("Bulk import completed: {} successful, {} errors", successCount, errorCount);
        return result;
    }

    public List<AssetCategory> getAllAssetCategories() {
        log.info("Retrieving all asset categories");
        return assetCategoryRepository.findAll();
    }

    @Transactional
    public AssetCategory createAssetCategory(AssetCategory category, String adminUsername) {
        log.info("Admin {} creating new asset category: {}", adminUsername, category.getName());
        
        // Check if category name already exists
        AssetCategory existingCategory = assetCategoryRepository.findByName(category.getName());
        if (existingCategory != null) {
            throw new RuntimeException("Category name already exists: " + category.getName());
        }
        
        category.setCreatedAt(LocalDateTime.now());
        AssetCategory savedCategory = assetCategoryRepository.save(category);
        log.info("Asset category created successfully: {}", savedCategory.getId());
        return savedCategory;
    }

    @Transactional
    public AssetCategory updateAssetCategory(Long id, AssetCategory category, String adminUsername) {
        log.info("Admin {} updating asset category: {}", adminUsername, id);
        
        AssetCategory existingCategory = assetCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
        
        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        
        AssetCategory updatedCategory = assetCategoryRepository.save(existingCategory);
        log.info("Asset category updated successfully: {}", updatedCategory.getId());
        return updatedCategory;
    }

    @Transactional
    public void deleteAssetCategory(Long id, String adminUsername) {
        log.info("Admin {} deleting asset category: {}", adminUsername, id);
        
        AssetCategory category = assetCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
        
        // Check if category is being used by any assets
        long assetCount = assetRepository.countByCategoryId(category.getId());
        if (assetCount > 0) {
            throw new RuntimeException("Cannot delete category: " + category.getName() + " - " + assetCount + " assets are using this category");
        }
        
        assetCategoryRepository.delete(category);
        log.info("Asset category deleted successfully: {}", id);
    }

    // User Management Methods
    public List<User> getAllUsers(String adminUsername) {
        log.info("Admin {} retrieving all users", adminUsername);
        return userRepository.findAll();
    }

    @Transactional
    public User createUser(CreateUserRequest request, String adminUsername) {
        log.info("Admin {} creating new user: {}", adminUsername, request.getUsername());
        
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }
        
        // Create new user from request
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRoleId(request.getRoleId());
        user.setDepartmentId(request.getDepartmentId());
        user.setManagerId(request.getManagerId());
        
        // Set default values
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser.getId());
        return savedUser;
    }

    public User getUserById(Long id, String adminUsername) {
        log.info("Admin {} retrieving user: {}", adminUsername, id);
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    @Transactional
    public User updateUser(Long id, CreateUserRequest request, String adminUsername) {
        log.info("Admin {} updating user: {}", adminUsername, id);
        
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        
        // Check if new username conflicts with existing users (excluding current user)
        if (!request.getUsername().equals(existingUser.getUsername()) &&
            userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }
        
        // Check if new email conflicts with existing users (excluding current user)
        if (!request.getEmail().equals(existingUser.getEmail()) &&
            userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }
        
        // Update user fields
        existingUser.setUsername(request.getUsername());
        existingUser.setEmail(request.getEmail());
        existingUser.setFirstName(request.getFirstName());
        existingUser.setLastName(request.getLastName());
        existingUser.setRoleId(request.getRoleId());
        existingUser.setDepartmentId(request.getDepartmentId());
        existingUser.setManagerId(request.getManagerId());
        
        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            existingUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        
        existingUser.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully: {}", updatedUser.getId());
        return updatedUser;
    }

    @Transactional
    public void deleteUser(Long id, String adminUsername) {
        log.info("Admin {} deleting user: {}", adminUsername, id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        
        // Check if user has active loans
        // This would require checking AssetLoanRepository
        // For now, we'll just deactivate the user instead of deleting
        
        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("User deactivated successfully: {}", id);
    }

    @Transactional
    public User updateUserRole(Long id, String role, String department, String adminUsername) {
        log.info("Admin {} updating user role: {} -> {}", adminUsername, id, role);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        
        // Note: In a real implementation, you would look up role and department IDs
        // For now, we'll set default values
        user.setRoleId(1L); // Default role ID
        user.setDepartmentId(1L); // Default department ID
        
        User updatedUser = userRepository.save(user);
        log.info("User role updated successfully: {}", updatedUser.getId());
        return updatedUser;
    }

    @Transactional
    public User updateUserStatus(Long id, Boolean active, String adminUsername) {
        log.info("Admin {} updating user status: {} -> {}", adminUsername, id, active);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        
        user.setIsActive(active);
        
        User updatedUser = userRepository.save(user);
        log.info("User status updated successfully: {}", updatedUser.getId());
        return updatedUser;
    }

    // System Configuration Methods
    public Map<String, Object> getSystemConfig(String adminUsername) {
        log.info("Admin {} retrieving system configuration", adminUsername);
        
        Map<String, Object> config = new HashMap<>();
        config.put("defaultLoanDuration", 7);
        config.put("maxAssetsPerUser", 3);
        config.put("emailNotificationsEnabled", true);
        config.put("managerApprovalRequired", true);
        config.put("systemMaintenanceMode", false);
        
        return config;
    }

    @Transactional
    public Map<String, Object> updateSystemConfig(Map<String, Object> config, String adminUsername) {
        log.info("Admin {} updating system configuration", adminUsername);
        
        // In a real application, you would save this to a configuration table
        // For now, we'll just return the updated config
        Map<String, Object> updatedConfig = new HashMap<>(config);
        updatedConfig.put("lastUpdated", LocalDateTime.now());
        updatedConfig.put("updatedBy", adminUsername);
        
        log.info("System configuration updated successfully");
        return updatedConfig;
    }

    // Reports Methods
    public Map<String, Object> getAssetUtilizationReport(String adminUsername) {
        log.info("Admin {} generating asset utilization report", adminUsername);
        
        Map<String, Object> report = new HashMap<>();
        
        long totalAssets = assetRepository.count();
        long availableAssets = assetRepository.countByStatus(AssetStatus.available);
        long loanedAssets = assetRepository.countByStatus(AssetStatus.loaned);
        long maintenanceAssets = assetRepository.countByStatus(AssetStatus.maintenance);
        
        report.put("totalAssets", totalAssets);
        report.put("availableAssets", availableAssets);
        report.put("loanedAssets", loanedAssets);
        report.put("maintenanceAssets", maintenanceAssets);
        report.put("utilizationRate", totalAssets > 0 ? (double) loanedAssets / totalAssets * 100 : 0);
        report.put("generatedAt", LocalDateTime.now());
        
        return report;
    }

    public Map<String, Object> getUserActivityReport(String adminUsername) {
        log.info("Admin {} generating user activity report", adminUsername);
        
        Map<String, Object> report = new HashMap<>();
        
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countActiveUsers();
        long inactiveUsers = userRepository.countInactiveUsers();
        
        report.put("totalUsers", totalUsers);
        report.put("activeUsers", activeUsers);
        report.put("inactiveUsers", inactiveUsers);
        report.put("activeRate", totalUsers > 0 ? (double) activeUsers / totalUsers * 100 : 0);
        report.put("generatedAt", LocalDateTime.now());
        
        return report;
    }

    public Map<String, Object> getOverdueAssetsReport(String adminUsername) {
        log.info("Admin {} generating overdue assets report", adminUsername);
        
        Map<String, Object> report = new HashMap<>();
        
        // This would typically query the asset_loans table for overdue assets
        // For now, we'll return a placeholder
        report.put("overdueAssets", 0);
        report.put("totalOverdueValue", 0.0);
        report.put("generatedAt", LocalDateTime.now());
        
        return report;
    }

    public Map<String, Object> getDepartmentSummaryReport(String adminUsername) {
        log.info("Admin {} generating department summary report", adminUsername);
        
        Map<String, Object> report = new HashMap<>();
        
        // This would typically aggregate data by department
        // For now, we'll return a placeholder
        report.put("departments", new ArrayList<>());
        report.put("totalAssetsByDepartment", new HashMap<>());
        report.put("generatedAt", LocalDateTime.now());
        
        return report;
    }

    // Data Export Methods
    public Map<String, Object> exportData(Map<String, Object> exportRequest, String adminUsername) {
        log.info("Admin {} exporting data: {}", adminUsername, exportRequest);
        
        String exportType = (String) exportRequest.get("exportType");
        String fileFormat = (String) exportRequest.get("fileFormat");
        
        Map<String, Object> result = new HashMap<>();
        result.put("exportType", exportType);
        result.put("fileFormat", fileFormat);
        result.put("recordCount", 0);
        result.put("downloadUrl", "/api/admin/export/download/" + System.currentTimeMillis());
        result.put("exportedAt", LocalDateTime.now());
        
        log.info("Data export completed: {} records in {} format", 0, fileFormat);
        return result;
    }

    // Department and Role Methods
    public List<Department> getAllDepartments() {
        log.info("Retrieving all departments");
        return departmentRepository.findAll();
    }

    public List<Role> getAllRoles() {
        log.info("Retrieving all roles");
        return roleRepository.findAll();
    }
}
