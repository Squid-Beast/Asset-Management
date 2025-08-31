package com.example.asset_management.repository;

import com.example.asset_management.model.Asset;
import com.example.asset_management.model.Asset.AssetStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends CrudRepository<Asset, Long> {
    
    @Query("SELECT * FROM assets WHERE asset_tag = :assetTag")
    Optional<Asset> findByAssetTag(@Param("assetTag") String assetTag);
    
    @Query("SELECT * FROM assets WHERE status = :status")
    List<Asset> findByStatus(@Param("status") AssetStatus status);
    
    @Query("SELECT * FROM assets WHERE category_id = :categoryId")
    List<Asset> findByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT * FROM assets WHERE status = :status AND category_id = :categoryId")
    List<Asset> findByStatusAndCategoryId(@Param("status") AssetStatus status, @Param("categoryId") Long categoryId);
    
    @Query("SELECT * FROM assets WHERE status = 'AVAILABLE' ORDER BY name")
    List<Asset> findAvailableAssets();
    
    @Query("SELECT * FROM assets ORDER BY asset_tag")
    List<Asset> findAllOrderedByAssetTag();
    
    @Query("SELECT EXISTS(SELECT 1 FROM assets WHERE asset_tag = :assetTag)")
    boolean existsByAssetTag(@Param("assetTag") String assetTag);
    
    @Query("SELECT * FROM assets WHERE id = :id")
    Optional<Asset> findById(@Param("id") Long id);
    
    @Query("SELECT * FROM assets WHERE id IN (:ids)")
    List<Asset> findAllById(@Param("ids") List<Long> ids);
}
