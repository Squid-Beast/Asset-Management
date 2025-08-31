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
public class Role {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
}
