package com.example.asset_management.service;

import com.example.asset_management.model.Notification;
import com.example.asset_management.model.User;
import com.example.asset_management.repository.NotificationRepository;
import com.example.asset_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createNotification(Long userId, String title, String message, Notification.NotificationType type, Long relatedLoanId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .relatedLoanId(relatedLoanId)
                .build();
        
        notificationRepository.save(notification);
        log.info("Created notification for user {} - {}: {}", userId, title, message);
    }

    public void sendAssetAssignedNotification(String userEmail, String assetName, String dueDate) {
        log.info("NOTIFICATION: Asset '{}' has been assigned to {}. Due date: {}", 
                assetName, userEmail, dueDate);
        // In a real implementation, this would send an email
    }

    public void sendAssetDueSoonNotification(String userEmail, String assetName, String dueDate) {
        log.info("NOTIFICATION: Asset '{}' assigned to {} is due soon on {}", 
                assetName, userEmail, dueDate);
        // In a real implementation, this would send an email
    }

    public void sendAssetOverdueNotification(String userEmail, String assetName, String dueDate) {
        log.warn("NOTIFICATION: Asset '{}' assigned to {} is OVERDUE since {}", 
                assetName, userEmail, dueDate);
        // In a real implementation, this would send an urgent email
    }

    public void sendAssetReturnedNotification(String userEmail, String assetName) {
        log.info("NOTIFICATION: Asset '{}' has been returned by {}", 
                assetName, userEmail);
        // In a real implementation, this would send a confirmation email
    }

    public void sendApprovalRequestNotification(String managerEmail, String requesterName, String assetName) {
        log.info("NOTIFICATION: Manager {} needs to approve loan request for asset '{}' by {}", 
                managerEmail, assetName, requesterName);
        // In a real implementation, this would send an approval request email
    }

    @Transactional
    public void notifyLoanApproved(Long userId, String assetName, String assetTag, Long loanId) {
        createNotification(
            userId,
            "Loan Approved",
            String.format("Your loan request for %s (%s) has been approved!", assetName, assetTag),
            Notification.NotificationType.LOAN_APPROVED,
            loanId
        );
    }

    @Transactional
    public void notifyLoanRejected(Long userId, String assetName, String assetTag, Long loanId) {
        createNotification(
            userId,
            "Loan Rejected",
            String.format("Your loan request for %s (%s) has been rejected.", assetName, assetTag),
            Notification.NotificationType.LOAN_REJECTED,
            loanId
        );
    }

    @Transactional
    public void notifyNewLoanRequest(Long managerId, String requesterName, String assetName, String assetTag, Long loanId) {
        createNotification(
            managerId,
            "New Loan Request",
            String.format("%s has requested to borrow %s (%s)", requesterName, assetName, assetTag),
            Notification.NotificationType.LOAN_REQUEST_RECEIVED,
            loanId
        );
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        log.debug("Marking notification {} as read", notificationId);
        notificationRepository.markAsReadById(notificationId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        log.debug("Marking all notifications as read for user {}", userId);
        notificationRepository.markAllAsReadByUserId(userId);
    }
}
