package com.example.asset_management.repository;

import com.example.asset_management.model.Asset;
import com.example.asset_management.model.Asset.AssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    
    @Query("SELECT a FROM Asset a WHERE a.assetTag = :assetTag")
    Optional<Asset> findByAssetTag(@Param("assetTag") String assetTag);
    
    @Query("SELECT a FROM Asset a WHERE a.status = :status")
    List<Asset> findByStatus(@Param("status") AssetStatus status);
    
    @Query("SELECT a FROM Asset a WHERE a.categoryId = :categoryId")
    List<Asset> findByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT a FROM Asset a WHERE a.status = :status AND a.categoryId = :categoryId")
    List<Asset> findByStatusAndCategoryId(@Param("status") AssetStatus status, @Param("categoryId") Long categoryId);
    
    @Query("SELECT a FROM Asset a WHERE a.status = 'available' ORDER BY a.name")
    List<Asset> findAvailableAssets();
    
    @Query("SELECT a FROM Asset a ORDER BY a.assetTag")
    List<Asset> findAllOrderedByAssetTag();
    
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.status = :status")
    long countByStatus(@Param("status") AssetStatus status);
    
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.categoryId = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);
    
    boolean existsByAssetTag(String assetTag);
}