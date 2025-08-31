package com.example.asset_management.dto;

import lombok.Getter;
import lombok.Setter;
import com.example.asset_management.model.AssetLoan.LoanStatus;
import java.time.LocalDateTime;

@Getter
@Setter
public class AssetLoanResponse {
    private Long id;
    private Long assetId;
    private String assetTag;
    private String assetName;
    private String userName;
    private String assignedByName;
    private LoanStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime dueAt;
    private LocalDateTime returnedAt;
    private String damageNote;
    private LocalDateTime createdAt;
}