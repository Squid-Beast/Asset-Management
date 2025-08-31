package com.example.asset_management.controller;

import com.example.asset_management.dto.ApiResponse;
import com.example.asset_management.dto.AssetAssignmentRequest;
import com.example.asset_management.dto.AssetLoanResponse;
import com.example.asset_management.dto.AssetReturnRequest;
import com.example.asset_management.service.AssetLoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

    private final AssetLoanService assetLoanService;

    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<AssetLoanResponse>> assignAsset(
            @Valid @RequestBody AssetAssignmentRequest request,
            Authentication authentication) {
        try {
            AssetLoanResponse response = assetLoanService.assignAsset(request, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Asset assigned successfully", response));
        } catch (Exception e) {
            log.error("Failed to assign asset", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{loanId}/approve")
    public ResponseEntity<ApiResponse<AssetLoanResponse>> approveLoan(
            @PathVariable Long loanId,
            Authentication authentication) {
        try {
            AssetLoanResponse response = assetLoanService.approveLoan(loanId, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Loan approved successfully", response));
        } catch (Exception e) {
            log.error("Failed to approve loan: {}", loanId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/return")
    public ResponseEntity<ApiResponse<AssetLoanResponse>> returnAsset(
            @Valid @RequestBody AssetReturnRequest request,
            Authentication authentication) {
        try {
            AssetLoanResponse response = assetLoanService.returnAsset(request, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Asset returned successfully", response));
        } catch (Exception e) {
            log.error("Failed to return asset", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/my-loans")
    public ResponseEntity<ApiResponse<List<AssetLoanResponse>>> getMyLoans(Authentication authentication) {
        try {
            List<AssetLoanResponse> loans = assetLoanService.getMyLoans(authentication.getName());
            return ResponseEntity.ok(ApiResponse.success(loans));
        } catch (Exception e) {
            log.error("Failed to get user loans", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to retrieve loans"));
        }
    }

    @GetMapping("/pending-approvals")
    public ResponseEntity<ApiResponse<List<AssetLoanResponse>>> getPendingApprovals(Authentication authentication) {
        try {
            List<AssetLoanResponse> pendingLoans = assetLoanService.getPendingApprovals();
            return ResponseEntity.ok(ApiResponse.success(pendingLoans));
        } catch (Exception e) {
            log.error("Failed to get pending approvals", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/team-loans")
    public ResponseEntity<ApiResponse<List<AssetLoanResponse>>> getTeamLoans(Authentication authentication) {
        try {
            List<AssetLoanResponse> teamLoans = assetLoanService.getTeamLoans(authentication.getName());
            return ResponseEntity.ok(ApiResponse.success(teamLoans));
        } catch (Exception e) {
            log.error("Failed to get team loans", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
