package com.example.asset_management.controller;

import com.example.asset_management.dto.ApiResponse;
import com.example.asset_management.dto.AssetAssignmentRequest;
import com.example.asset_management.dto.AssetResponse;
import com.example.asset_management.dto.AssetReturnRequest;
import com.example.asset_management.dto.CreateAssetRequest;
import com.example.asset_management.exception.AssetNotFoundException;
import com.example.asset_management.model.Asset;
import com.example.asset_management.model.Asset.AssetStatus;
import com.example.asset_management.service.AssetService;
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
@RequestMapping("/assets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AssetResponse>>> getAllAssets() {
        try {
            List<AssetResponse> assets = assetService.getAllAssets();
            return ResponseEntity.ok(ApiResponse.success(assets));
        } catch (Exception e) {
            log.error("Failed to get all assets", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to retrieve assets"));
        }
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<AssetResponse>>> getAvailableAssets() {
        try {
            List<AssetResponse> assets = assetService.getAvailableAssets();
            return ResponseEntity.ok(ApiResponse.success(assets));
        } catch (Exception e) {
            log.error("Failed to get available assets", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to retrieve assets"));
        }
    }



    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetResponse>> getAssetById(@PathVariable Long id) {
        try {
            AssetResponse asset = assetService.getAssetById(id);
            return ResponseEntity.ok(ApiResponse.success(asset));
        } catch (AssetNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to get asset by ID: {}", id, e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to retrieve asset"));
        }
    }

    @GetMapping("/tag/{assetTag}")
    public ResponseEntity<ApiResponse<AssetResponse>> getAssetByTag(@PathVariable String assetTag) {
        try {
            AssetResponse asset = assetService.getAssetByTag(assetTag);
            return ResponseEntity.ok(ApiResponse.success(asset));
        } catch (AssetNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to get asset by tag: {}", assetTag, e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to retrieve asset"));
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAssetStatistics() {
        try {
            Map<String, Object> statistics = assetService.getAssetStatistics();
            return ResponseEntity.ok(ApiResponse.success("Asset statistics retrieved successfully", statistics));
        } catch (Exception e) {
            log.error("Failed to get asset statistics", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AssetResponse>> createAsset(
            @Valid @RequestBody CreateAssetRequest request,
            Authentication authentication) {
        try {
            AssetResponse createdAsset = assetService.createAssetFromRequest(request);
            return ResponseEntity.ok(ApiResponse.success("Asset created successfully", createdAsset));
        } catch (Exception e) {
            log.error("Failed to create asset", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetResponse>> updateAsset(
            @PathVariable Long id,
            @Valid @RequestBody CreateAssetRequest request,
            Authentication authentication) {
        try {
            AssetResponse updatedAsset = assetService.updateAssetFromRequest(id, request);
            return ResponseEntity.ok(ApiResponse.success("Asset updated successfully", updatedAsset));
        } catch (AssetNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to update asset: {}", id, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAsset(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            assetService.deleteAsset(id);
            return ResponseEntity.ok(ApiResponse.success("Asset deleted successfully", null));
        } catch (AssetNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to delete asset: {}", id, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AssetResponse>> updateAssetStatus(
            @PathVariable Long id,
            @RequestParam AssetStatus status,
            Authentication authentication) {
        try {
            AssetResponse updatedAsset = assetService.updateAssetStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("Asset status updated successfully", updatedAsset));
        } catch (AssetNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to update asset status: {} -> {}", id, status, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<String>> assignAsset(
            @Valid @RequestBody AssetAssignmentRequest request,
            Authentication authentication) {
        try {
            // This will be handled by AssetLoanService
            return ResponseEntity.ok(ApiResponse.success("Asset assignment request submitted", null));
        } catch (Exception e) {
            log.error("Failed to assign asset", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/return")
    public ResponseEntity<ApiResponse<String>> returnAsset(
            @Valid @RequestBody AssetReturnRequest request,
            Authentication authentication) {
        try {
            // This will be handled by AssetLoanService
            return ResponseEntity.ok(ApiResponse.success("Asset return request submitted", null));
        } catch (Exception e) {
            log.error("Failed to return asset", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
