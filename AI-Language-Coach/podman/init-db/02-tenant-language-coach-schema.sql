-- ============================================================================
-- AI-Language-Coach: Tenant Schema - Language Coach
-- Phase 1 - Database Schema
-- ============================================================================
-- Run this in the language_coach database:
--   psql -U infra_admin -d language_coach -f 02-tenant-language-coach-schema.sql
-- ============================================================================

-- 1. Core Identity: Users Table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    provider VARCHAR(50) DEFAULT 'authentik',
    tenant_id VARCHAR(50) NOT NULL DEFAULT '1',
    enabled BOOLEAN DEFAULT true,
    account_non_expired BOOLEAN DEFAULT true,
    account_non_locked BOOLEAN DEFAULT true,
    credentials_non_expired BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_email_tenant UNIQUE (email, tenant_id)
);

-- 2. Configuration: Languages Table (Target Languages to Learn)
CREATE TABLE IF NOT EXISTS languages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    language_code VARCHAR(10) NOT NULL,
    language_name VARCHAR(100) NOT NULL,
    level VARCHAR(10) NOT NULL,
    display_name VARCHAR(100),
    enabled BOOLEAN DEFAULT true,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 3. Logic: Diagnostic Questions Table
CREATE TABLE IF NOT EXISTS diagnostic_questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    target_language VARCHAR(50) NOT NULL,
    target_level VARCHAR(10) NOT NULL,
    native_language VARCHAR(10),
    question_text TEXT,
    situation TEXT,
    options TEXT,
    correct_answer VARCHAR(255),
    explanation TEXT,
    question_type VARCHAR(50),
    linguistic_bridge_en TEXT,
    linguistic_bridge_bn TEXT,
    linguistic_bridge_hi TEXT,
    linguistic_bridge_te TEXT,
    linguistic_bridge_uk TEXT,
    active BOOLEAN DEFAULT true,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 4. Helper: Native Languages Table (User's Native Language)
CREATE TABLE IF NOT EXISTS native_languages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    language_code VARCHAR(10) UNIQUE NOT NULL,
    language_name VARCHAR(100) NOT NULL,
    native_name VARCHAR(100),
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 5. System: Configuration
CREATE TABLE IF NOT EXISTS system_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT NOT NULL,
    description TEXT,
    editable BOOLEAN DEFAULT true,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 6. System: Plan Types
CREATE TABLE IF NOT EXISTS plan_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_code VARCHAR(20) UNIQUE NOT NULL,
    plan_name VARCHAR(50) NOT NULL,
    requests_per_minute INTEGER DEFAULT 10,
    voice_minutes INTEGER DEFAULT 60,
    llm_model VARCHAR(50),
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_tenant_id ON users(tenant_id);
CREATE INDEX IF NOT EXISTS idx_languages_code_level ON languages(language_code, level);
CREATE INDEX IF NOT EXISTS idx_questions_lang_level ON diagnostic_questions(target_language, target_level);
CREATE INDEX IF NOT EXISTS idx_questions_active ON diagnostic_questions(active);