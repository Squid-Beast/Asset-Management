package com.example.asset_management.repository;

import com.example.asset_management.model.AssetCategory;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetCategoryRepository extends CrudRepository<AssetCategory, Long> {
    
    @Query("SELECT * FROM asset_categories WHERE name = :name")
    AssetCategory findByName(@Param("name") String name);
    
    @Query("SELECT * FROM asset_categories ORDER BY name")
    List<AssetCategory> findAllOrderedByName();
    
    @Query("SELECT * FROM asset_categories WHERE id = :id")
    Optional<AssetCategory> findById(@Param("id") Long id);
    
    @Query("SELECT * FROM asset_categories WHERE id IN (:ids)")
    List<AssetCategory> findAllById(@Param("ids") List<Long> ids);
}
