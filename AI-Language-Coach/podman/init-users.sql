-- ============================================================
-- Users Table Schema for Multi-Tenant Application
-- Run this in each tenant's database
-- ============================================================

-- Tenant 1: language_coach database
-- ============================================================

-- Drop existing table if exists (for fresh setup)
-- DROP TABLE IF EXISTS users CASCADE;

-- Create users table for tenant 1 (language-coach)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255),  -- null for OAuth2/Authentik users
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    provider VARCHAR(50) DEFAULT 'authentik',  -- 'local', 'authentik', 'google', etc.
    tenant_id VARCHAR(50) NOT NULL DEFAULT '1',
    enabled BOOLEAN DEFAULT true,
    account_non_expired BOOLEAN DEFAULT true,
    account_non_locked BOOLEAN DEFAULT true,
    credentials_non_expired BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_email_tenant UNIQUE (email, tenant_id)
);

-- Index for efficient tenant-scoped lookups
CREATE INDEX IF NOT EXISTS idx_users_email_tenant ON users(email, tenant_id);
CREATE INDEX IF NOT EXISTS idx_users_tenant_id ON users(tenant_id);

-- ============================================================
-- Tenant 2: app2_db database (run separately)
-- ============================================================
/*
-- Run this in app2_db:
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    provider VARCHAR(50) DEFAULT 'authentik',
    tenant_id VARCHAR(50) NOT NULL DEFAULT '2',
    enabled BOOLEAN DEFAULT true,
    account_non_expired BOOLEAN DEFAULT true,
    account_non_locked BOOLEAN DEFAULT true,
    credentials_non_expired BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_email_tenant UNIQUE (email, tenant_id)
);

CREATE INDEX IF NOT EXISTS idx_users_email_tenant ON users(email, tenant_id);
CREATE INDEX IF NOT EXISTS idx_users_tenant_id ON users(tenant_id);
*/

-- ============================================================
-- Sample Data (optional - for testing)
-- ============================================================

-- Insert test user for tenant 1 (password is 'password123' hashed with BCrypt)
-- Note: In production, users are created via Authentik OAuth2 flow
/*
INSERT INTO users (email, password, first_name, last_name, provider, tenant_id)
VALUES (
    'testuser@coach.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',  -- password123
    'Test',
    'User',
    'local',
    '1'
) ON CONFLICT DO NOTHING;
*/

-- ============================================================
-- Column Descriptions
-- ============================================================
-- id: UUID primary key
-- email: User's email (unique per tenant)
-- password: BCrypt hashed password (null for OAuth2 users)
-- first_name: User's first name
-- last_name: User's last name
-- provider: Authentication provider ('local', 'authentik', 'google')
-- tenant_id: Tenant identifier (used for multi-tenancy)
-- enabled: Is user account active
-- account_non_expired: Has user account expired
-- account_non_locked: Is user account locked
-- credentials_non_expired: Has password expired
-- created_at: Account creation timestamp
-- updated_at: Last update timestamp