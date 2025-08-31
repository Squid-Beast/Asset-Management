package com.example.asset_management.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "assets")
public class Asset {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "asset_tag")
    private String assetTag;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "category_id")
    private Long categoryId;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private AssetStatus status;
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;
    @Column(name = "warranty_expiry")
    private LocalDate warrantyExpiry;
    @Column(name = "notes")
    private String notes;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum AssetStatus {
        available,
        loaned,
        maintenance,
        retired
    }
}