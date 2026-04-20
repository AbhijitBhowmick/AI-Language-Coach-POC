-- 1. Connect to the specific tenant database
\c language_coach;

-- 2. Core Identity: Users Table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255),  -- NULL for Authentik/OAuth2 users
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

-- 3. Configuration: Languages Table
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

-- 4. Logic: Diagnostic Questions Table
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

-- 5. Helper: Native Languages Table
CREATE TABLE IF NOT EXISTS native_languages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    language_code VARCHAR(10) UNIQUE NOT NULL,
    language_name VARCHAR(100) NOT NULL,
    native_name VARCHAR(100),
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 6. System: Configuration & Plans
CREATE TABLE IF NOT EXISTS system_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS plan_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_code VARCHAR(20) UNIQUE NOT NULL,
    plan_name VARCHAR(50) NOT NULL,
    requests_per_minute INTEGER DEFAULT 10,
    llm_model VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_tenant_id ON users(tenant_id);
CREATE INDEX IF NOT EXISTS idx_questions_lang_level ON diagnostic_questions(target_language, target_level);

--------------------
-- 1. Connect to the tenant
\c language_coach;

-- 2. Seed Languages (Using codes as unique markers to prevent duplication)
INSERT INTO languages (language_code, language_name, level, display_name, display_order)
VALUES
    ('cs', 'Czech', 'A1', 'Czech A1', 1),
    ('cs', 'Czech', 'A2', 'Czech A2', 2),
    ('de', 'German', 'A1', 'German A1', 11),
    ('nl', 'Dutch', 'A1', 'Dutch A1', 21)
ON CONFLICT DO NOTHING;

-- 3. Seed Native Languages
INSERT INTO native_languages (language_code, language_name, native_name)
VALUES
    ('en', 'English', 'English'),
    ('bn', 'Bengali', 'বাংলা'),
    ('hi', 'Hindi', 'हिन्दी'),
    ('te', 'Telugu', 'తెలుగు'),
    ('uk', 'Ukrainian', 'Українська')
ON CONFLICT DO NOTHING;

-- 4. Seed Questions
INSERT INTO diagnostic_questions (target_language, target_level, native_language, question_text, correct_answer, question_type, linguistic_bridge_en)
VALUES
    ('Czech', 'A1', 'en', '___ (Já) mám rád/ráda češtinu.', 'Já', 'GRAMMAR_COMPLETION', 'I = Já'),
    ('German', 'A1', 'en', '___ bin Student.', 'Ich', 'GRAMMAR_COMPLETION', 'I = Ich'),
    ('Dutch', 'A1', 'en', 'Ik ___ een student.', 'ben', 'GRAMMAR_COMPLETION', 'I am = Ik ben')
ON CONFLICT DO NOTHING;

-- 5. Seed Plans
INSERT INTO plan_types (plan_code, plan_name, llm_model)
VALUES
    ('FREE', 'Free Tier', 'gemini-flash'),
    ('PREMIUM', 'Premium Tier', 'gemini-pro')
ON CONFLICT DO NOTHING;
-------------------