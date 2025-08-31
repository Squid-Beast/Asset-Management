package com.example.asset_management.dto;

import lombok.Getter;
import lombok.Setter;
import com.example.asset_management.model.Asset.AssetStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class AssetResponse {
    private Long id;
    private String assetTag;
    private String name;
    private String description;
    private String categoryName;
    private AssetStatus status;
    private LocalDate purchaseDate;
    private LocalDate warrantyExpiry;
    private String notes;
    private LocalDateTime createdAt;
}