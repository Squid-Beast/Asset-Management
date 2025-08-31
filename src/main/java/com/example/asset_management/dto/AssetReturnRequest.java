package com.example.asset_management.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class AssetReturnRequest {
    
    @NotNull(message = "Asset ID is required")
    private Long assetId;
    
    private String damageNote;
}