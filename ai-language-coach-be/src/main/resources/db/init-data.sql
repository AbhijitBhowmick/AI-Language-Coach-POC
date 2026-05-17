-- 1. Connect to the specific tenant database
\c language_coach;

-- 2. Core Identity: Users Table (Updated with roles and approval)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    provider VARCHAR(50) DEFAULT 'local',
    tenant_id VARCHAR(50) NOT NULL DEFAULT '1',
    status VARCHAR(30) DEFAULT 'PENDING_APPROVAL',
    business_name VARCHAR(255),
    created_by UUID REFERENCES users(id),
    approved_by UUID REFERENCES users(id),
    approved_at TIMESTAMP,
    approval_comment TEXT,
    enabled BOOLEAN DEFAULT true,
    account_non_expired BOOLEAN DEFAULT true,
    account_non_locked BOOLEAN DEFAULT true,
    credentials_non_expired BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_email_tenant UNIQUE (email, tenant_id)
);

-- 2.1 User Roles Table (Enhanced)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(30) NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_users_tenant ON users(tenant_id);
CREATE INDEX IF NOT EXISTS idx_users_created_by ON users(created_by);

-- 2.2 Approval Requests Table
CREATE TABLE IF NOT EXISTS approval_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    requested_role VARCHAR(30) NOT NULL,
    requested_by UUID NOT NULL REFERENCES users(id),
    status VARCHAR(30) DEFAULT 'PENDING',
    approved_by UUID REFERENCES users(id),
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    created_at TIMESTAMP DEFAULT NOW()
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

-- =====================================================
-- GAMIFICATION TABLES (Phase 1)
-- =====================================================

-- Game Templates Registry (dynamic - add new games without restart)
CREATE TABLE IF NOT EXISTS game_templates (
    template_id VARCHAR(50) PRIMARY KEY,
    display_name VARCHAR(100) NOT NULL,
    template_category VARCHAR(30) NOT NULL,
    display_order INTEGER DEFAULT 0,
    icon_class VARCHAR(50),
    description TEXT,
    default_time_seconds INTEGER DEFAULT 30,
    points_per_correct INTEGER DEFAULT 10,
    penalty_points INTEGER DEFAULT 0,
    lives_enabled BOOLEAN DEFAULT FALSE,
    branching_enabled BOOLEAN DEFAULT FALSE,
    min_questions INTEGER DEFAULT 4,
    max_questions INTEGER DEFAULT 20,
    config_schema JSONB DEFAULT '{}',
    difficulty_weights JSONB DEFAULT '{}',
    skill_areas JSONB DEFAULT '[]',
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Seed default game templates (Wordwall, LearningApps, Genially styles)
INSERT INTO game_templates (template_id, display_name, template_category, display_order, icon_class, description, 
    default_time_seconds, points_per_correct, penalty_points, lives_enabled, branching_enabled, 
    min_questions, max_questions, skill_areas) VALUES
('standard', 'Quiz', 'GRAMMAR', 1, 'fa-question-circle', 'Multiple choice questions',
    30, 10, 0, FALSE, FALSE, 5, 20, '["reading","writing"]'),
('match_up', 'Match Up', 'VOCAB', 2, 'fa-puzzle-piece', 'Drag and drop matching pairs',
    60, 15, -5, FALSE, FALSE, 6, 16, '["reading","vocabulary"]'),
('cloze_text', 'Fill in the Blank', 'GRAMMAR', 3, 'fa-pen-fancy', 'Gap-fill text completion',
    45, 12, -3, FALSE, FALSE, 3, 15, '["reading","writing"]'),
('anagram', 'Anagram', 'VOCAB', 4, 'fa-font', 'Unscramble the word or phrase',
    30, 15, -5, TRUE, FALSE, 5, 15, '["spelling"]'),
('whack_mole', 'Whack-a-Mole', 'VOCAB', 5, 'fa-bug', 'Hit the correct answer fast',
    20, 20, -10, TRUE, FALSE, 10, 20, '["listening","recognition"]'),
('speaking_card', 'Speaking Card', 'SPEAKING', 6, 'fa-microphone', 'Record your spoken response',
    90, 25, 0, FALSE, FALSE, 1, 10, '["speaking"]'),
('situational_branching', 'Scenario', 'LISTENING', 7, 'fa-users', 'Branching conversation scenario',
    120, 30, 0, FALSE, TRUE, 3, 10, '["listening","speaking"]'),
('group_sort', 'Group Sort', 'GRAMMAR', 8, 'fa-layer-group', 'Drag items to correct categories',
    60, 15, -5, FALSE, FALSE, 6, 15, '["grammar","vocabulary"]')
ON CONFLICT (template_id) DO NOTHING;

-- Game Sessions (per user game state)
CREATE TABLE IF NOT EXISTS game_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    target_language VARCHAR(50) NOT NULL,
    target_level VARCHAR(10) NOT NULL,
    template_id VARCHAR(50) NOT NULL,
    score INTEGER DEFAULT 0,
    streak_count INTEGER DEFAULT 0,
    lives_remaining INTEGER DEFAULT 3,
    accuracy_percent DECIMAL(5,2) DEFAULT 0,
    total_questions INTEGER DEFAULT 0,
    correct_answers INTEGER DEFAULT 0,
    response_time_ms INTEGER DEFAULT 0,
    best_time_ms INTEGER,
    started_at TIMESTAMP DEFAULT NOW(),
    completed_at TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES game_templates(template_id)
);

-- Interaction History (for IRT adaptive learning)
CREATE TABLE IF NOT EXISTS interaction_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    question_id UUID,
    template_id VARCHAR(50) NOT NULL,
    success BOOLEAN NOT NULL,
    response_time_ms INTEGER,
    difficulty_weight DECIMAL(5,2),
    ability_estimate DECIMAL(5,4),
    points_earned INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (template_id) REFERENCES game_templates(template_id)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_game_sessions_user ON game_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_interaction_history_user ON interaction_history(user_id);
CREATE INDEX IF NOT EXISTS idx_interaction_history_template ON interaction_history(template_id);

-- =====================================================
-- USER ROLES & ONBOARDING SEED DATA
-- =====================================================

-- Seed SUPER_ADMIN user (manual password set - CHANGE IMMEDIATELY)
-- Email: admin@platform.com, Password: ChangeMe123!
INSERT INTO users (id, email, password, first_name, last_name, provider, tenant_id, status, enabled)
VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'admin@platform.com',
    '$2a$12$aeWLnZcmKK0Y4j9f4C9Ah.3QGv/tKBX8MXx6p.9Um31tKo0s5GNnO',
    'Platform',
    'Administrator',
    'local',
    'SYSTEM',
    'ACTIVE',
    TRUE
)
ON CONFLICT DO NOTHING;

-- Update user_roles for super admin
INSERT INTO user_roles (user_id, role)
SELECT 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'SUPER_ADMIN'
WHERE EXISTS (SELECT 1 FROM users WHERE id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11')
ON CONFLICT DO NOTHING;

-- =====================================================
-- COMMUNITY SERVICE TABLES
-- =====================================================

-- Communities (each org can have multiple communities)
CREATE TABLE IF NOT EXISTS communities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    tenant_id VARCHAR(50) NOT NULL,
    created_by UUID NOT NULL,
    admin_teacher_id UUID NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Community Members
CREATE TABLE IF NOT EXISTS community_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    community_id UUID NOT NULL REFERENCES communities(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    role VARCHAR(20) DEFAULT 'MEMBER',
    blocked BOOLEAN DEFAULT FALSE,
    joined_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(community_id, user_id)
);

-- Community Posts
CREATE TABLE IF NOT EXISTS community_posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    community_id UUID NOT NULL REFERENCES communities(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    content TEXT NOT NULL,
    likes_count INTEGER DEFAULT 0,
    comments_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Community Comments
CREATE TABLE IF NOT EXISTS community_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL REFERENCES community_posts(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Community Activities (for leaderboard - auto-aggregated from voice/game/diagnostic)
CREATE TABLE IF NOT EXISTS community_activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    type VARCHAR(30) NOT NULL,
    score INTEGER DEFAULT 0,
    metadata JSONB,
    community_id UUID REFERENCES communities(id),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Indexes for community service
CREATE INDEX IF NOT EXISTS idx_communities_tenant ON communities(tenant_id);
CREATE INDEX IF NOT EXISTS idx_communities_admin ON communities(admin_teacher_id);
CREATE INDEX IF NOT EXISTS idx_community_members_community ON community_members(community_id);
CREATE INDEX IF NOT EXISTS idx_community_members_user ON community_members(user_id);
CREATE INDEX IF NOT EXISTS idx_community_posts_community ON community_posts(community_id);
CREATE INDEX IF NOT EXISTS idx_community_posts_user ON community_posts(user_id);
CREATE INDEX IF NOT EXISTS idx_community_comments_post ON community_comments(post_id);
CREATE INDEX IF NOT EXISTS idx_community_activities_user ON community_activities(user_id);
CREATE INDEX IF NOT EXISTS idx_community_activities_tenant ON community_activities(tenant_id);
CREATE INDEX IF NOT EXISTS idx_community_activities_community ON community_activities(community_id);