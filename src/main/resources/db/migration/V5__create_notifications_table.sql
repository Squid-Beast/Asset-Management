-- Create notifications table
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type ENUM('LOAN_APPROVED', 'LOAN_REJECTED', 'LOAN_REQUEST_RECEIVED', 'ASSET_DUE_SOON', 'ASSET_OVERDUE', 'GENERAL') NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    related_loan_id BIGINT,
    INDEX idx_user_id (user_id),
    INDEX idx_user_id_read (user_id, is_read),
    INDEX idx_created_at (created_at)
);
