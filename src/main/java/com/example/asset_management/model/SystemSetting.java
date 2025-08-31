package com.example.asset_management.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemSetting {
    private Long id;
    private String settingKey;
    private String settingValue;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
