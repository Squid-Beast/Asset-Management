package com.example.asset_management.repository;

import com.example.asset_management.model.AssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetCategoryRepository extends JpaRepository<AssetCategory, Long> {
    
    @Query("SELECT ac FROM AssetCategory ac WHERE ac.name = :name")
    AssetCategory findByName(@Param("name") String name);
    
    @Query("SELECT ac FROM AssetCategory ac ORDER BY ac.name")
    List<AssetCategory> findAllOrderedByName();
    
    boolean existsByName(String name);
}