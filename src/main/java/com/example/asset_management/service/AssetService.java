package com.example.asset_management.service;

import com.example.asset_management.dto.AssetResponse;
import com.example.asset_management.model.Asset;
import com.example.asset_management.model.Asset.AssetStatus;
import com.example.asset_management.model.AssetCategory;
import com.example.asset_management.repository.AssetCategoryRepository;
import com.example.asset_management.repository.AssetRepository;
import com.example.asset_management.exception.AssetNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetCategoryRepository assetCategoryRepository;

    public List<AssetResponse> getAvailableAssets() {
        List<Asset> assets = assetRepository.findAvailableAssets();
        return mapToAssetResponses(assets);
    }

    public List<AssetResponse> getAllAssets() {
        List<Asset> assets = assetRepository.findAllOrderedByAssetTag();
        return mapToAssetResponses(assets);
    }

    public AssetResponse getAssetById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new AssetNotFoundException(id));
        return mapToAssetResponse(asset);
    }

    public AssetResponse getAssetByTag(String assetTag) {
        Asset asset = assetRepository.findByAssetTag(assetTag)
                .orElseThrow(() -> new AssetNotFoundException(assetTag, "tag"));
        return mapToAssetResponse(asset);
    }

    public List<AssetResponse> getAssetsByCategory(Long categoryId) {
        List<Asset> assets = assetRepository.findByCategoryId(categoryId);
        return mapToAssetResponses(assets);
    }

    public List<AssetResponse> getAssetsByStatus(AssetStatus status) {
        List<Asset> assets = assetRepository.findByStatus(status);
        return mapToAssetResponses(assets);
    }

    @Transactional
    public AssetResponse createAsset(Asset asset) {
        // Validate asset tag uniqueness
        if (assetRepository.existsByAssetTag(asset.getAssetTag())) {
            throw new RuntimeException("Asset tag already exists: " + asset.getAssetTag());
        }

        // Validate category exists
        if (!assetCategoryRepository.findById(asset.getCategoryId()).isPresent()) {
            throw new RuntimeException("Asset category not found: " + asset.getCategoryId());
        }

        Asset savedAsset = assetRepository.save(asset);
        log.info("Asset created: {}", savedAsset.getAssetTag());
        
        return mapToAssetResponse(savedAsset);
    }

    @Transactional
    public AssetResponse updateAsset(Long id, Asset asset) {
        Asset existingAsset = assetRepository.findById(id)
                .orElseThrow(() -> new AssetNotFoundException(id));

        // Check if asset tag is being changed and if it's unique
        if (!existingAsset.getAssetTag().equals(asset.getAssetTag()) && 
            assetRepository.existsByAssetTag(asset.getAssetTag())) {
            throw new RuntimeException("Asset tag already exists: " + asset.getAssetTag());
        }

        // Update fields
        existingAsset.setName(asset.getName());
        existingAsset.setDescription(asset.getDescription());
        existingAsset.setCategoryId(asset.getCategoryId());
        existingAsset.setStatus(asset.getStatus());
        existingAsset.setPurchaseDate(asset.getPurchaseDate());
        existingAsset.setWarrantyExpiry(asset.getWarrantyExpiry());
        existingAsset.setNotes(asset.getNotes());

        Asset savedAsset = assetRepository.save(existingAsset);
        log.info("Asset updated: {}", savedAsset.getAssetTag());
        
        return mapToAssetResponse(savedAsset);
    }

    @Transactional
    public void deleteAsset(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new AssetNotFoundException(id));

        // Check if asset is currently loaned
        if (asset.getStatus() == AssetStatus.loaned) {
            throw new RuntimeException("Cannot delete asset that is currently loaned");
        }

        assetRepository.deleteById(id);
        log.info("Asset deleted: {}", asset.getAssetTag());
    }

    @Transactional
    public AssetResponse updateAssetStatus(Long id, AssetStatus status) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new AssetNotFoundException(id));

        asset.setStatus(status);
        Asset savedAsset = assetRepository.save(asset);
        log.info("Asset status updated: {} -> {}", asset.getAssetTag(), status);
        
        return mapToAssetResponse(savedAsset);
    }

    /**
     * Optimized method to map multiple assets to responses, avoiding N+1 queries
     */
    private List<AssetResponse> mapToAssetResponses(List<Asset> assets) {
        if (assets.isEmpty()) {
            return List.of();
        }

        // Batch load all category IDs
        List<Long> categoryIds = assets.stream()
                .map(Asset::getCategoryId)
                .distinct()
                .collect(Collectors.toList());

        // Load all categories in one query
        Map<Long, String> categoryMap = assetCategoryRepository.findAllById(categoryIds)
                .stream()
                .collect(Collectors.toMap(
                        AssetCategory::getId,
                        AssetCategory::getName
                ));

        return assets.stream()
                .map(asset -> mapToAssetResponse(asset, categoryMap))
                .collect(Collectors.toList());
    }

    /**
     * Optimized single asset mapping with pre-loaded categories
     */
    private AssetResponse mapToAssetResponse(Asset asset) {
        String categoryName = assetCategoryRepository.findById(asset.getCategoryId())
                .map(AssetCategory::getName)
                .orElse("Unknown");

        return mapToAssetResponse(asset, Map.of(asset.getCategoryId(), categoryName));
    }

    /**
     * Core mapping method using pre-loaded category data
     */
    private AssetResponse mapToAssetResponse(Asset asset, Map<Long, String> categoryMap) {
        String categoryName = categoryMap.getOrDefault(asset.getCategoryId(), "Unknown");

        AssetResponse response = new AssetResponse();
        response.setId(asset.getId());
        response.setAssetTag(asset.getAssetTag());
        response.setName(asset.getName());
        response.setDescription(asset.getDescription());
        response.setCategoryName(categoryName);
        response.setStatus(asset.getStatus());
        response.setPurchaseDate(asset.getPurchaseDate());
        response.setWarrantyExpiry(asset.getWarrantyExpiry());
        response.setNotes(asset.getNotes());
        response.setCreatedAt(asset.getCreatedAt());
        return response;
    }
}
