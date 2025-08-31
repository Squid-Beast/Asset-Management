package com.example.asset_management.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    
    private Loan loan = new Loan();
    private Notification notification = new Notification();
    
    @Data
    public static class Loan {
        private int approvalThresholdDays = 7;
        private int dueReminderDays = 2;
    }
    
    @Data
    public static class Notification {
        private int maxRetryAttempts = 3;
        private int retryDelaySeconds = 60;
    }
}
