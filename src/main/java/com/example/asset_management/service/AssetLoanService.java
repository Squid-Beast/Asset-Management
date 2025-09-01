package com.example.asset_management.service;

import com.example.asset_management.dto.AssetAssignmentRequest;
import com.example.asset_management.dto.AssetLoanResponse;
import com.example.asset_management.dto.AssetReturnRequest;
import com.example.asset_management.dto.NotificationMessage;
import com.example.asset_management.service.NotificationService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final KafkaEventService kafkaEventService;
    private final WebSocketNotificationService webSocketNotificationService;
    private final NotificationService notificationService;

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

        // Update asset status only if immediately approved
        if (status == LoanStatus.loaned) {
            asset.setStatus(AssetStatus.loaned);
            assetRepository.save(asset);
            
            // Publish events
            eventService.publishAssetAssignedEvent(savedLoan);
            kafkaEventService.publishAssetAssignedEvent(savedLoan);
            
            // Send immediate WebSocket notification
            webSocketNotificationService.sendAssetEventNotification(
                user.getUsername(),
                "AssetAssigned",
                asset.getName(),
                String.format("Asset '%s' has been assigned to you. Due date: %s", 
                    asset.getName(), request.getDueAt())
            );
        } else if (status == LoanStatus.pending_approval) {
            // Keep asset available until approved
            // Send approval request notification to managers
            webSocketNotificationService.sendApprovalRequestNotification(
                user.getFirstName() + " " + user.getLastName(),
                asset.getName()
            );
            
            // Send confirmation to employee that request is pending approval
            webSocketNotificationService.sendLoanStatusNotification(
                user.getUsername(),
                "Pending Approval",
                asset.getName(),
                String.format("Your request for asset '%s' has been submitted and is pending manager approval", asset.getName())
            );
            
            // Create notification for managers about new loan request
            // Find all managers and notify them
            List<User> managers = userRepository.findByRoleName("MANAGER");
            for (User manager : managers) {
                notificationService.notifyNewLoanRequest(
                    manager.getId(),
                    user.getFirstName() + " " + user.getLastName(),
                    asset.getName(),
                    asset.getAssetTag(),
                    savedLoan.getId()
                );
            }
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

        // Get asset info and update its status
        Asset asset = assetRepository.findById(loan.getAssetId())
                .orElseThrow(() -> new RuntimeException("Asset not found"));
        asset.setStatus(AssetStatus.loaned);
        assetRepository.save(asset);

        // Create notification for the employee
        notificationService.notifyLoanApproved(loan.getUserId(), asset.getName(), asset.getAssetTag(), loan.getId());

        // Publish events
        eventService.publishAssetAssignedEvent(savedLoan);
        kafkaEventService.publishAssetAssignedEvent(savedLoan);

        // Send notifications
        User loanUser = userRepository.findById(loan.getUserId()).orElse(null);
        
        if (loanUser != null && asset != null) {
            // Notify employee that their request was approved
            webSocketNotificationService.sendLoanStatusNotification(
                loanUser.getUsername(),
                "Approved",
                asset.getName(),
                String.format("Your request for asset '%s' has been approved by %s", 
                    asset.getName(), approver.getFirstName() + " " + approver.getLastName())
            );
            
            // Notify other managers that the approval was handled
            webSocketNotificationService.sendNotificationToManagers(
                NotificationMessage.builder()
                    .type("APPROVAL_COMPLETED")
                    .title("Loan Approved")
                    .message(String.format("%s approved %s's request for asset '%s'", 
                        approver.getFirstName() + " " + approver.getLastName(),
                        loanUser.getFirstName() + " " + loanUser.getLastName(),
                        asset.getName()))
                    .severity("success")
                    .build()
            );
        }

        log.info("Loan {} approved by manager {}", loanId, approverUsername);
        
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

        // Publish events
        eventService.publishAssetReturnedEvent(savedLoan);
        kafkaEventService.publishAssetReturnedEvent(savedLoan);
        
        // Send immediate WebSocket notification
        webSocketNotificationService.sendAssetEventNotification(
            user.getUsername(),
            "AssetReturned",
            asset.getName(),
            String.format("Asset '%s' has been returned successfully", asset.getName())
        );

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

    @Transactional
    public AssetLoanResponse rejectLoan(Long loanId, String rejectorUsername) {
        User rejector = userRepository.findActiveByUsername(rejectorUsername)
                .orElseThrow(() -> new RuntimeException("Rejector not found"));

        if (!authService.isManager()) {
            throw new RuntimeException("Only managers can reject loans");
        }

        AssetLoan loan = assetLoanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getStatus() != LoanStatus.pending_approval) {
            throw new RuntimeException("Only pending approval loans can be rejected");
        }

        // Get asset info before updating
        Asset asset = assetRepository.findById(loan.getAssetId())
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        // Create notification for the employee before updating
        notificationService.notifyLoanRejected(loan.getUserId(), asset.getName(), asset.getAssetTag(), loan.getId());

        // Update loan status to rejected (keep for audit trail)
        loan.setStatus(LoanStatus.rejected);
        AssetLoan savedLoan = assetLoanRepository.save(loan);

        // Update asset status back to available
        asset.setStatus(AssetStatus.available);
        assetRepository.save(asset);

        // Publish events
        eventService.publishAssetRejectedEvent(savedLoan);
        kafkaEventService.publishAssetRejectedEvent(savedLoan);
        
        // Send immediate WebSocket notification
        User loanUser = userRepository.findById(loan.getUserId()).orElse(null);
        
        if (loanUser != null && asset != null) {
            // Notify employee that their request was rejected
            webSocketNotificationService.sendLoanStatusNotification(
                loanUser.getUsername(),
                "Rejected",
                asset.getName(),
                String.format("Your request for asset '%s' has been rejected by %s", 
                    asset.getName(), rejector.getFirstName() + " " + rejector.getLastName())
            );
            
            // Notify other managers that the rejection was handled
            webSocketNotificationService.sendNotificationToManagers(
                NotificationMessage.builder()
                    .type("REJECTION_COMPLETED")
                    .title("Loan Rejected")
                    .message(String.format("%s rejected %s's request for asset '%s'", 
                        rejector.getFirstName() + " " + rejector.getLastName(),
                        loanUser.getFirstName() + " " + loanUser.getLastName(),
                        asset.getName()))
                    .severity("warning")
                    .build()
            );
        }

        log.info("Loan {} rejected by manager {}", loanId, rejectorUsername);
        
        return mapToAssetLoanResponse(savedLoan);
    }

    public Map<String, Object> getLoanStatistics(String username) {
        User user = userRepository.findActiveByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> statistics = new HashMap<>();
        
        if (authService.isManager()) {
            // Manager statistics
            List<User> subordinates = userRepository.findSubordinatesByManagerId(user.getId());
            List<Long> subordinateIds = subordinates.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            
            if (!subordinateIds.isEmpty()) {
                List<AssetLoan> teamLoans = assetLoanRepository.findByUserIdIn(subordinateIds);
                
                statistics.put("totalTeamLoans", teamLoans.size());
                statistics.put("pendingApprovals", teamLoans.stream()
                        .filter(loan -> loan.getStatus() == LoanStatus.pending_approval)
                        .count());
                statistics.put("activeLoans", teamLoans.stream()
                        .filter(loan -> loan.getStatus() == LoanStatus.loaned)
                        .count());
                statistics.put("overdueLoans", teamLoans.stream()
                        .filter(loan -> loan.getStatus() == LoanStatus.overdue)
                        .count());
            } else {
                statistics.put("totalTeamLoans", 0);
                statistics.put("pendingApprovals", 0);
                statistics.put("activeLoans", 0);
                statistics.put("overdueLoans", 0);
            }
        } else {
            // Employee statistics
            List<AssetLoan> userLoans = assetLoanRepository.findByUserId(user.getId());
            
            statistics.put("totalLoans", userLoans.size());
            statistics.put("pendingLoans", userLoans.stream()
                    .filter(loan -> loan.getStatus() == LoanStatus.pending_approval)
                    .count());
            statistics.put("activeLoans", userLoans.stream()
                    .filter(loan -> loan.getStatus() == LoanStatus.loaned)
                    .count());
            statistics.put("overdueLoans", userLoans.stream()
                    .filter(loan -> loan.getStatus() == LoanStatus.overdue)
                    .count());
        }
        
        // Get available assets count
        long availableAssets = assetRepository.countByStatus(AssetStatus.available);
        statistics.put("availableAssets", availableAssets);
        
        return statistics;
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
