-- Insert Super Admin first (no manager)
INSERT INTO users (username, email, first_name, last_name, password_hash, role_id, department_id, manager_id) VALUES
('adm01', 'admin@company.com', 'Super', 'Admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 
 (SELECT id FROM roles WHERE name = 'SUPER_ADMIN'), 
 (SELECT id FROM departments WHERE name = 'IT Department'), 
 NULL);

-- Set variables for user IDs
SET @admin_id = (SELECT id FROM users WHERE username = 'adm01');

-- Insert Managers (reporting to Super Admin)
INSERT INTO users (username, email, first_name, last_name, password_hash, role_id, department_id, manager_id) VALUES
('mgr01', 'manager1@company.com', 'John', 'Manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 
 (SELECT id FROM roles WHERE name = 'MANAGER'), 
 (SELECT id FROM departments WHERE name = 'IT Department'), 
 @admin_id),
('mgr02', 'manager2@company.com', 'Sarah', 'Director', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 
 (SELECT id FROM roles WHERE name = 'MANAGER'), 
 (SELECT id FROM departments WHERE name = 'HR Department'), 
 @admin_id);

-- Set variables for manager IDs
SET @manager1_id = (SELECT id FROM users WHERE username = 'mgr01');
SET @manager2_id = (SELECT id FROM users WHERE username = 'mgr02');

-- Insert Employees (reporting to respective managers)
INSERT INTO users (username, email, first_name, last_name, password_hash, role_id, department_id, manager_id) VALUES
-- Employees under Manager 1 (IT Department)
('emp01', 'employee1@company.com', 'Alice', 'Johnson', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 
 (SELECT id FROM roles WHERE name = 'EMPLOYEE'), 
 (SELECT id FROM departments WHERE name = 'IT Department'), 
 @manager1_id),
('emp02', 'employee2@company.com', 'Bob', 'Smith', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 
 (SELECT id FROM roles WHERE name = 'EMPLOYEE'), 
 (SELECT id FROM departments WHERE name = 'IT Department'), 
 @manager1_id),
-- Employees under Manager 2 (HR Department)
('emp03', 'employee3@company.com', 'Charlie', 'Brown', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 
 (SELECT id FROM roles WHERE name = 'EMPLOYEE'), 
 (SELECT id FROM departments WHERE name = 'HR Department'), 
 @manager2_id),
('emp04', 'employee4@company.com', 'Diana', 'Wilson', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 
 (SELECT id FROM roles WHERE name = 'EMPLOYEE'), 
 (SELECT id FROM departments WHERE name = 'HR Department'), 
 @manager2_id),
('emp05', 'employee5@company.com', 'Eve', 'Davis', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 
 (SELECT id FROM roles WHERE name = 'EMPLOYEE'), 
 (SELECT id FROM departments WHERE name = 'Finance Department'), 
 @manager2_id);

-- Insert sample assets
INSERT INTO assets (asset_tag, name, description, category_id, status, purchase_date, warranty_expiry, notes) VALUES
-- Laptops
('LAP001', 'Dell Latitude 5520', 'Business laptop with Intel i7, 16GB RAM, 512GB SSD', 
 (SELECT id FROM asset_categories WHERE name = 'Laptop'), 'available', '2025-01-15', '2028-01-15', 'Excellent condition'),
('LAP002', 'HP EliteBook 840', 'Premium business laptop with Intel i5, 8GB RAM, 256GB SSD', 
 (SELECT id FROM asset_categories WHERE name = 'Laptop'), 'available', '2025-03-20', '2028-03-20', 'Good condition'),
('LAP003', 'MacBook Pro 14"', 'Apple MacBook Pro with M2 chip, 16GB RAM, 512GB SSD', 
 (SELECT id FROM asset_categories WHERE name = 'Laptop'), 'available', '2025-06-10', '2028-06-10', 'Like new'),
('LAP004', 'Lenovo ThinkPad X1', 'Ultrabook with Intel i7, 16GB RAM, 1TB SSD', 
 (SELECT id FROM asset_categories WHERE name = 'Laptop'), 'loaned', '2025-02-28', '2028-02-28', 'Currently assigned'),
('LAP005', 'Dell XPS 13', 'Developer laptop with Intel i7, 32GB RAM, 1TB SSD', 
 (SELECT id FROM asset_categories WHERE name = 'Laptop'), 'maintenance', '2025-04-12', '2028-04-12', 'Under repair'),

-- Badges
('BAD001', 'Access Card - Alice Johnson', 'RFID access card for building entry', 
 (SELECT id FROM asset_categories WHERE name = 'Badge'), 'loaned', '2025-01-10', '2027-01-10', 'Assigned to Alice'),
('BAD002', 'Access Card - Bob Smith', 'RFID access card for building entry', 
 (SELECT id FROM asset_categories WHERE name = 'Badge'), 'loaned', '2025-01-10', '2027-01-10', 'Assigned to Bob'),
('BAD003', 'Access Card - Charlie Brown', 'RFID access card for building entry', 
 (SELECT id FROM asset_categories WHERE name = 'Badge'), 'loaned', '2025-01-10', '2027-01-10', 'Assigned to Charlie'),
('BAD004', 'Access Card - Diana Wilson', 'RFID access card for building entry', 
 (SELECT id FROM asset_categories WHERE name = 'Badge'), 'loaned', '2025-01-10', '2027-01-10', 'Assigned to Diana'),
('BAD005', 'Access Card - Eve Davis', 'RFID access card for building entry', 
 (SELECT id FROM asset_categories WHERE name = 'Badge'), 'loaned', '2025-01-10', '2027-01-10', 'Assigned to Eve'),
('BAD006', 'Access Card - Manager 1', 'RFID access card for building entry', 
 (SELECT id FROM asset_categories WHERE name = 'Badge'), 'loaned', '2025-01-10', '2027-01-10', 'Assigned to John Manager'),
('BAD007', 'Access Card - Manager 2', 'RFID access card for building entry', 
 (SELECT id FROM asset_categories WHERE name = 'Badge'), 'loaned', '2025-01-10', '2027-01-10', 'Assigned to Sarah Director'),
('BAD008', 'Access Card - Admin', 'RFID access card for building entry', 
 (SELECT id FROM asset_categories WHERE name = 'Badge'), 'loaned', '2025-01-10', '2027-01-10', 'Assigned to Super Admin'),
('BAD009', 'Visitor Badge 1', 'Temporary visitor access card', 
 (SELECT id FROM asset_categories WHERE name = 'Badge'), 'available', '2025-01-10', '2027-01-10', 'For visitors'),
('BAD010', 'Visitor Badge 2', 'Temporary visitor access card', 
 (SELECT id FROM asset_categories WHERE name = 'Badge'), 'available', '2025-01-10', '2027-01-10', 'For visitors'),

-- Monitors
('MON001', 'Dell 27" 4K Monitor', 'Ultra HD monitor with USB-C connectivity', 
 (SELECT id FROM asset_categories WHERE name = 'Monitor'), 'available', '2025-05-15', '2028-05-15', 'Excellent display quality'),
('MON002', 'HP 24" Business Monitor', 'Standard business monitor with VGA/HDMI', 
 (SELECT id FROM asset_categories WHERE name = 'Monitor'), 'available', '2025-05-15', '2028-05-15', 'Good for office use'),
('MON003', 'LG 32" Curved Monitor', 'Curved gaming monitor with high refresh rate', 
 (SELECT id FROM asset_categories WHERE name = 'Monitor'), 'loaned', '2025-05-15', '2028-05-15', 'Currently assigned'),
('MON004', 'Samsung 27" Monitor', 'Standard office monitor', 
 (SELECT id FROM asset_categories WHERE name = 'Monitor'), 'available', '2025-05-15', '2028-05-15', 'Available for assignment'),

-- Peripherals
('PER001', 'Logitech MX Master 3', 'Wireless ergonomic mouse', 
 (SELECT id FROM asset_categories WHERE name = 'Peripheral'), 'available', '2025-07-01', '2027-07-01', 'Premium mouse'),
('PER002', 'Microsoft Ergonomic Keyboard', 'Split ergonomic keyboard', 
 (SELECT id FROM asset_categories WHERE name = 'Peripheral'), 'available', '2025-07-01', '2027-07-01', 'Good for long typing'),
('PER003', 'Apple Magic Mouse', 'Wireless mouse for Mac users', 
 (SELECT id FROM asset_categories WHERE name = 'Peripheral'), 'loaned', '2025-07-01', '2027-07-01', 'Currently assigned'),
('PER004', 'Dell Wireless Keyboard', 'Standard wireless keyboard', 
 (SELECT id FROM asset_categories WHERE name = 'Peripheral'), 'available', '2025-07-01', '2027-07-01', 'Available for assignment'),

-- Mobile Devices
('MOB001', 'iPad Pro 12.9"', 'Apple iPad Pro with M2 chip, 256GB storage', 
 (SELECT id FROM asset_categories WHERE name = 'Mobile Device'), 'available', '2025-08-01', '2028-08-01', 'For presentations'),
('MOB002', 'Samsung Galaxy Tab S9', 'Android tablet with 128GB storage', 
 (SELECT id FROM asset_categories WHERE name = 'Mobile Device'), 'available', '2025-08-01', '2028-08-01', 'For field work'),
('MOB003', 'iPhone 15 Pro', 'Apple iPhone 15 Pro, 256GB storage', 
 (SELECT id FROM asset_categories WHERE name = 'Mobile Device'), 'loaned', '2025-08-01', '2028-08-01', 'Currently assigned'),
('MOB004', 'Samsung Galaxy S24', 'Android smartphone, 128GB storage', 
 (SELECT id FROM asset_categories WHERE name = 'Mobile Device'), 'available', '2025-08-01', '2028-08-01', 'Available for assignment');

-- Set variables for user and asset IDs for loan creation
SET @alice_id = (SELECT id FROM users WHERE username = 'emp01');
SET @bob_id = (SELECT id FROM users WHERE username = 'emp02');
SET @charlie_id = (SELECT id FROM users WHERE username = 'emp03');
SET @diana_id = (SELECT id FROM users WHERE username = 'emp04');
SET @eve_id = (SELECT id FROM users WHERE username = 'emp05');
SET @john_manager_id = (SELECT id FROM users WHERE username = 'mgr01');
SET @sarah_manager_id = (SELECT id FROM users WHERE username = 'mgr02');
SET @admin_id = (SELECT id FROM users WHERE username = 'adm01');

SET @lap001_id = (SELECT id FROM assets WHERE asset_tag = 'LAP001');
SET @lap004_id = (SELECT id FROM assets WHERE asset_tag = 'LAP004');
SET @mon003_id = (SELECT id FROM assets WHERE asset_tag = 'MON003');
SET @per003_id = (SELECT id FROM assets WHERE asset_tag = 'PER003');
SET @mob003_id = (SELECT id FROM assets WHERE asset_tag = 'MOB003');

-- Insert sample asset loans
INSERT INTO asset_loans (asset_id, user_id, assigned_by_id, status, requested_at, approved_at, due_at) VALUES
-- Active loans
(@lap004_id, @alice_id, @john_manager_id, 'loaned', '2025-01-15 09:00:00', '2025-01-15 09:30:00', '2025-02-15 17:00:00'),
(@mon003_id, @bob_id, @john_manager_id, 'loaned', '2025-01-20 10:00:00', '2025-01-20 10:15:00', '2025-02-20 17:00:00'),
(@per003_id, @charlie_id, @sarah_manager_id, 'loaned', '2025-01-25 11:00:00', '2025-01-25 11:20:00', '2025-02-25 17:00:00'),
(@mob003_id, @diana_id, @sarah_manager_id, 'loaned', '2025-01-30 14:00:00', '2025-01-30 14:10:00', '2025-02-28 17:00:00'),

-- Pending approval (long-term loans)
(@lap001_id, @eve_id, @eve_id, 'pending_approval', '2025-02-01 15:00:00', NULL, '2025-03-15 17:00:00');

-- Insert calendar events for due dates
INSERT INTO calendar_events (title, description, user_id, asset_loan_id, event_type, start_at, end_at, status) VALUES
('Asset Due: LAP004 - Dell Latitude 5520', 'Alice Johnson needs to return Dell Latitude 5520', @alice_id, 
 (SELECT id FROM asset_loans WHERE asset_id = @lap004_id AND user_id = @alice_id), 'asset_due', 
 '2025-02-15 17:00:00', '2025-02-15 18:00:00', 'active'),

('Asset Due: MON003 - LG 32" Curved Monitor', 'Bob Smith needs to return LG 32" Curved Monitor', @bob_id,
 (SELECT id FROM asset_loans WHERE asset_id = @mon003_id AND user_id = @bob_id), 'asset_due',
 '2025-02-20 17:00:00', '2025-02-20 18:00:00', 'active'),

('Asset Due: PER003 - Apple Magic Mouse', 'Charlie Brown needs to return Apple Magic Mouse', @charlie_id,
 (SELECT id FROM asset_loans WHERE asset_id = @per003_id AND user_id = @charlie_id), 'asset_due',
 '2025-02-25 17:00:00', '2025-02-25 18:00:00', 'active'),

('Asset Due: MOB003 - iPhone 15 Pro', 'Diana Wilson needs to return iPhone 15 Pro', @diana_id,
 (SELECT id FROM asset_loans WHERE asset_id = @mob003_id AND user_id = @diana_id), 'asset_due',
 '2025-02-28 17:00:00', '2025-02-28 18:00:00', 'active');
