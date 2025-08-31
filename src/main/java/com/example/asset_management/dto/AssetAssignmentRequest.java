package com.example.asset_management.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import java.time.LocalDateTime;

@Getter
@Setter
public class AssetAssignmentRequest {
    
    @NotNull(message = "Asset ID is required")
    private Long assetId;
    
    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be in the future")
    private LocalDateTime dueAt;
    
    private String notes;
}