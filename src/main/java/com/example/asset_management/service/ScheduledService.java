package com.example.asset_management.service;

import com.example.asset_management.model.AssetLoan;
import com.example.asset_management.model.AssetLoan.LoanStatus;
import com.example.asset_management.repository.AssetLoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledService {

    private final AssetLoanRepository assetLoanRepository;
    private final EventService eventService;

    @Value("${app.due-reminder-days:2}")
    private int dueReminderDays;

    // Run daily at 9:00 AM
    @Scheduled(cron = "0 0 9 * * ?")
    public void scanDueDates() {
        log.info("Starting daily due date scan...");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(dueReminderDays);
        
        // Find loans due soon
        List<AssetLoan> dueSoonLoans = assetLoanRepository.findLoansDueSoon(now, futureDate);
        log.info("Found {} loans due soon", dueSoonLoans.size());
        
        for (AssetLoan loan : dueSoonLoans) {
            try {
                eventService.publishAssetDueSoonEvent(loan);
                log.info("Published AssetDueSoon event for loan ID: {}", loan.getId());
            } catch (Exception e) {
                log.error("Failed to publish AssetDueSoon event for loan ID: {}", loan.getId(), e);
            }
        }
        
        // Find overdue loans
        List<AssetLoan> overdueLoans = assetLoanRepository.findOverdueLoans(now);
        log.info("Found {} overdue loans", overdueLoans.size());
        
        for (AssetLoan loan : overdueLoans) {
            try {
                // Update loan status to overdue
                loan.setStatus(LoanStatus.OVERDUE);
                assetLoanRepository.save(loan);
                
                eventService.publishAssetOverdueEvent(loan);
                log.info("Published AssetOverdue event for loan ID: {}", loan.getId());
            } catch (Exception e) {
                log.error("Failed to publish AssetOverdue event for loan ID: {}", loan.getId(), e);
            }
        }
        
        log.info("Daily due date scan completed");
    }

    // Run every 5 minutes to process outbox events
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void processOutboxEvents() {
        log.debug("Processing outbox events...");
        // This would be implemented to send events to Kafka
        // For now, we'll just log that it's running
    }
}
