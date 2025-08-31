-- Add performance indexes for repository queries
-- This migration adds indexes to optimize query performance

-- Asset Categories indexes
CREATE INDEX idx_asset_categories_name ON asset_categories(name);

-- Assets indexes
CREATE INDEX idx_assets_asset_tag ON assets(asset_tag);
CREATE INDEX idx_assets_status ON assets(status);
CREATE INDEX idx_assets_category_id ON assets(category_id);
CREATE INDEX idx_assets_status_category ON assets(status, category_id);
CREATE INDEX idx_assets_name ON assets(name);

-- Asset Loans indexes
CREATE INDEX idx_asset_loans_user_id ON asset_loans(user_id);
CREATE INDEX idx_asset_loans_asset_id ON asset_loans(asset_id);
CREATE INDEX idx_asset_loans_status ON asset_loans(status);
CREATE INDEX idx_asset_loans_asset_status ON asset_loans(asset_id, status);
CREATE INDEX idx_asset_loans_user_status ON asset_loans(user_id, status);
CREATE INDEX idx_asset_loans_due_at ON asset_loans(due_at);
CREATE INDEX idx_asset_loans_due_status ON asset_loans(due_at, status);
CREATE INDEX idx_asset_loans_assigned_by ON asset_loans(assigned_by_id);

-- Users indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_users_department_id ON users(department_id);
CREATE INDEX idx_users_manager_id ON users(manager_id);
CREATE INDEX idx_users_active ON users(is_active);
CREATE INDEX idx_users_active_username ON users(is_active, username);

-- Calendar Events indexes
CREATE INDEX idx_calendar_events_user_id ON calendar_events(user_id);
CREATE INDEX idx_calendar_events_asset_loan_id ON calendar_events(asset_loan_id);
CREATE INDEX idx_calendar_events_start_at ON calendar_events(start_at);
CREATE INDEX idx_calendar_events_status ON calendar_events(status);
CREATE INDEX idx_calendar_events_user_start ON calendar_events(user_id, start_at);

-- Departments indexes
CREATE INDEX idx_departments_name ON departments(name);

-- Roles indexes
CREATE INDEX idx_roles_name ON roles(name);
