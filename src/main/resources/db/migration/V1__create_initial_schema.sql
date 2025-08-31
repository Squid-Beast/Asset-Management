-- Create departments table
CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create roles table
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create users table with proper normalization
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    department_id BIGINT,
    manager_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (manager_id) REFERENCES users(id)
);

-- Create asset categories table
CREATE TABLE asset_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create assets table
CREATE TABLE assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_tag VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category_id BIGINT NOT NULL,
    status ENUM('available', 'loaned', 'maintenance', 'retired') DEFAULT 'available',
    purchase_date DATE,
    warranty_expiry DATE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES asset_categories(id)
);

-- Create asset loans table
CREATE TABLE asset_loans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    assigned_by_id BIGINT NOT NULL,
    status ENUM('pending_approval', 'loaned', 'returned', 'overdue') DEFAULT 'pending_approval',
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP NULL,
    due_at TIMESTAMP NOT NULL,
    returned_at TIMESTAMP NULL,
    damage_note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (asset_id) REFERENCES assets(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (assigned_by_id) REFERENCES users(id)
);

-- Create calendar events table
CREATE TABLE calendar_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    user_id BIGINT NOT NULL,
    asset_loan_id BIGINT,
    event_type ENUM('asset_due', 'asset_overdue', 'asset_returned', 'reminder') NOT NULL,
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    status ENUM('active', 'completed', 'cancelled') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (asset_loan_id) REFERENCES asset_loans(id)
);

-- Create outbox events table for Kafka integration
CREATE TABLE outbox_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload_json JSON NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP NULL,
    retry_count INT DEFAULT 0,
    INDEX idx_aggregate (aggregate_type, aggregate_id),
    INDEX idx_unsent (sent_at, retry_count)
);

-- Create system settings table
CREATE TABLE system_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert initial data
INSERT INTO departments (name, description) VALUES
('IT Department', 'Information Technology Department'),
('HR Department', 'Human Resources Department'),
('Finance Department', 'Finance and Accounting Department');

INSERT INTO roles (name, description) VALUES
('EMPLOYEE', 'Regular employee who can request and return assets'),
('MANAGER', 'Manager who can approve requests and manage team assets'),
('SUPER_ADMIN', 'Super administrator with full system access');

INSERT INTO asset_categories (name, description) VALUES
('Laptop', 'Portable computers and laptops'),
('Badge', 'Access cards and identification badges'),
('Monitor', 'Computer monitors and displays'),
('Peripheral', 'Keyboards, mice, and other peripherals'),
('Mobile Device', 'Tablets and mobile devices');

-- Insert system settings
INSERT INTO system_settings (setting_key, setting_value, description) VALUES
('loan_approval_threshold_days', '7', 'Number of days after which loan approval is required'),
('due_reminder_days', '2', 'Days before due date to send reminder'),
('max_retry_attempts', '3', 'Maximum retry attempts for failed notifications'),
('default_password', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'Default encrypted password (admin123)');
