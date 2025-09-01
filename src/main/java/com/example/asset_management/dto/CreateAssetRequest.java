package com.example.asset_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateAssetRequest {
    @NotBlank(message = "Asset tag is required")
    private String assetTag;
    
    @NotBlank(message = "Asset name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    @NotBlank(message = "Status is required")
    private String status;
    
    private LocalDate purchaseDate;
    
    private LocalDate warrantyExpiry;
    
    private String notes;
}
