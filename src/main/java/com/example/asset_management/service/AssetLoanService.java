package com.example.asset_management.service;

import com.example.asset_management.dto.AssetAssignmentRequest;
import com.example.asset_management.dto.AssetLoanResponse;
import com.example.asset_management.dto.AssetReturnRequest;
import com.example.asset_management.model.Asset;
import com.example.asset_management.model.Asset.AssetStatus;
import com.example.asset_management.model.AssetLoan;
import com.example.asset_management.model.AssetLoan.LoanStatus;
import com.example.asset_management.model.User;
import com.example.asset_management.repository.AssetLoanRepository;
import com.example.asset_management.repository.AssetRepository;
import com.example.asset_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class AssetLoanService {

    private final AssetLoanRepository assetLoanRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final EventService eventService;

    @Value("${app.loan.approval-threshold-days:7}")
    private int approvalThresholdDays;

    @Transactional
    public AssetLoanResponse assignAsset(AssetAssignmentRequest request, String username) {
        User user = userRepository.findActiveByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        // Check if asset is available
        if (asset.getStatus() != AssetStatus.available) {
            throw new RuntimeException("Asset is not available for assignment");
        }

        // Check if asset is already loaned
        Optional<AssetLoan> existingLoan = assetLoanRepository.findActiveLoanByAssetId(asset.getId());
        if (existingLoan.isPresent()) {
            throw new RuntimeException("Asset is already assigned to another user");
        }

        // Calculate loan duration
        long daysBetween = ChronoUnit.DAYS.between(LocalDateTime.now(), request.getDueAt());
        
        // Determine if approval is needed
        LoanStatus status = daysBetween > approvalThresholdDays ? 
                LoanStatus.pending_approval : LoanStatus.loaned;

        AssetLoan loan = new AssetLoan();
        loan.setAssetId(asset.getId());
        loan.setUserId(user.getId());
        loan.setAssignedById(user.getId()); // Self-assignment for now
        loan.setStatus(status);
        loan.setRequestedAt(LocalDateTime.now());
        loan.setApprovedAt(status == LoanStatus.loaned ? LocalDateTime.now() : null);
        loan.setDueAt(request.getDueAt());

        AssetLoan savedLoan = assetLoanRepository.save(loan);

        // Update asset status
        asset.setStatus(AssetStatus.loaned);
        assetRepository.save(asset);

        // Publish event
        if (status == LoanStatus.loaned) {
            eventService.publishAssetAssignedEvent(savedLoan);
        }

        log.info("Asset {} assigned to user {} with status {}", asset.getAssetTag(), username, status);
        
        return mapToAssetLoanResponse(savedLoan);
    }

    @Transactional
    public AssetLoanResponse approveLoan(Long loanId, String approverUsername) {
        User approver = userRepository.findActiveByUsername(approverUsername)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        if (!authService.isManager()) {
            throw new RuntimeException("Only managers can approve loans");
        }

        AssetLoan loan = assetLoanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getStatus() != LoanStatus.pending_approval) {
            throw new RuntimeException("Loan is not pending approval");
        }

        loan.setStatus(LoanStatus.loaned);
        loan.setApprovedAt(LocalDateTime.now());
        AssetLoan savedLoan = assetLoanRepository.save(loan);

        // Publish event
        eventService.publishAssetAssignedEvent(savedLoan);

        log.info("Loan {} approved by {}", loanId, approverUsername);
        
        return mapToAssetLoanResponse(savedLoan);
    }

    @Transactional
    public AssetLoanResponse returnAsset(AssetReturnRequest request, String username) {
        User user = userRepository.findActiveByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        AssetLoan loan = assetLoanRepository.findActiveLoanByAssetId(asset.getId())
                .orElseThrow(() -> new RuntimeException("No active loan found for this asset"));

        // Check if user owns this loan or is a manager
        if (!loan.getUserId().equals(user.getId()) && !authService.isManager()) {
            throw new RuntimeException("You can only return assets assigned to you");
        }

        loan.setStatus(LoanStatus.returned);
        loan.setReturnedAt(LocalDateTime.now());
        loan.setDamageNote(request.getDamageNote());
        AssetLoan savedLoan = assetLoanRepository.save(loan);

        // Update asset status
        asset.setStatus(AssetStatus.available);
        assetRepository.save(asset);

        // Publish event
        eventService.publishAssetReturnedEvent(savedLoan);

        log.info("Asset {} returned by user {}", asset.getAssetTag(), username);
        
        return mapToAssetLoanResponse(savedLoan);
    }

    public List<AssetLoanResponse> getMyLoans(String username) {
        User user = userRepository.findActiveByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<AssetLoan> loans = assetLoanRepository.findByUserId(user.getId());
        return loans.stream()
                .map(this::mapToAssetLoanResponse)
                .collect(Collectors.toList());
    }

    public List<AssetLoanResponse> getPendingApprovals() {
        if (!authService.isManager()) {
            throw new RuntimeException("Only managers can view pending approvals");
        }

        List<AssetLoan> pendingLoans = assetLoanRepository.findPendingApprovals();
        return pendingLoans.stream()
                .map(this::mapToAssetLoanResponse)
                .collect(Collectors.toList());
    }

    public List<AssetLoanResponse> getTeamLoans(String managerUsername) {
        User manager = userRepository.findActiveByUsername(managerUsername)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (!authService.isManager()) {
            throw new RuntimeException("Only managers can view team loans");
        }

        List<User> subordinates = userRepository.findSubordinatesByManagerId(manager.getId());
        List<Long> subordinateIds = subordinates.stream()
                .map(User::getId)
                .collect(Collectors.toList());

        List<AssetLoan> teamLoans = assetLoanRepository.findByUserIdIn(subordinateIds);
        return teamLoans.stream()
                .map(this::mapToAssetLoanResponse)
                .collect(Collectors.toList());
    }

    private AssetLoanResponse mapToAssetLoanResponse(AssetLoan loan) {
        Asset asset = assetRepository.findById(loan.getAssetId()).orElse(null);
        User user = userRepository.findById(loan.getUserId()).orElse(null);
        User assignedBy = userRepository.findById(loan.getAssignedById()).orElse(null);

        AssetLoanResponse response = new AssetLoanResponse();
        response.setId(loan.getId());
        response.setAssetId(loan.getAssetId());
        response.setAssetTag(asset != null ? asset.getAssetTag() : "Unknown");
        response.setAssetName(asset != null ? asset.getName() : "Unknown");
        response.setUserName(user != null ? user.getFirstName() + " " + user.getLastName() : "Unknown");
        response.setAssignedByName(assignedBy != null ? assignedBy.getFirstName() + " " + assignedBy.getLastName() : "Unknown");
        response.setStatus(loan.getStatus());
        response.setRequestedAt(loan.getRequestedAt());
        response.setApprovedAt(loan.getApprovedAt());
        response.setDueAt(loan.getDueAt());
        response.setReturnedAt(loan.getReturnedAt());
        response.setDamageNote(loan.getDamageNote());
        response.setCreatedAt(loan.getCreatedAt());
        return response;
    }
}
