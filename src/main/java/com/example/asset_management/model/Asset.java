package com.example.asset_management.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {
    private Long id;
    private String assetTag;
    private String name;
    private String description;
    private Long categoryId;
    private AssetStatus status;
    private LocalDate purchaseDate;
    private LocalDate warrantyExpiry;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Relationships
    private AssetCategory category;
    
    public enum AssetStatus {
        AVAILABLE,
        LOANED,
        MAINTENANCE,
        RETIRED
    }
}
