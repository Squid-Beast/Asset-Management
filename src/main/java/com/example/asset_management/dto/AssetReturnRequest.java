package com.example.asset_management.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetReturnRequest {
    
    @NotNull(message = "Asset ID is required")
    private Long assetId;
    
    private String damageNote;
}
