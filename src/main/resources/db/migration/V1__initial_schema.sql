-- CreatorOps PostgreSQL Initial Schema DDL Migration
-- Target Database: PostgreSQL 16
-- Conventions: BIGINT IDs, snake_case column names, VARCHAR enums, UTC timestamps

-- =========================================================================
-- TRIGGER FUNCTION DEFINITIONS
-- =========================================================================

-- Helper function to automatically update 'updated_at' columns on row changes
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =========================================================================
-- TABLE CREATION
-- =========================================================================

-- 1. organization table (Tenant root)
CREATE TABLE organization (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    logo_url VARCHAR(1024),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ
);

-- 2. user table (Users belonging to organization)
-- NOTE: Double quotes used for 'user' as it is a reserved keyword in SQL/PostgreSQL
CREATE TABLE "user" (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE RESTRICT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    image_url VARCHAR(1024),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. brand table (Multiple brands owned by organization)
CREATE TABLE brand (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE RESTRICT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    logo_url VARCHAR(1024),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ
);

-- 4. content table (Core content planning card)
CREATE TABLE content (
    id BIGSERIAL PRIMARY KEY,
    brand_id BIGINT NOT NULL REFERENCES brand(id) ON DELETE RESTRICT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    stage VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    due_date TIMESTAMPTZ,
    publish_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ
);

-- 5. assignment table (Assignments mapping contributors to content)
CREATE TABLE assignment (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT NOT NULL REFERENCES content(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE RESTRICT,
    role VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 6. task table (Lightweight checklist tasks under content cards)
CREATE TABLE task (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT NOT NULL REFERENCES content(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'TODO',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 7. comment table (Contextual discussion strings under content cards)
CREATE TABLE comment (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT NOT NULL REFERENCES content(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE RESTRICT,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 8. research_item table (Reference items linked to content)
CREATE TABLE research_item (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT NOT NULL REFERENCES content(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE RESTRICT,
    type VARCHAR(50) NOT NULL,
    content TEXT,
    url VARCHAR(1024),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 9. script table (Script container for content cards)
CREATE TABLE script (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT NOT NULL REFERENCES content(id) ON DELETE CASCADE,
    current_version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 10. script_version table (Script iteration version records)
CREATE TABLE script_version (
    id BIGSERIAL PRIMARY KEY,
    script_id BIGINT NOT NULL REFERENCES script(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE RESTRICT,
    version_number INT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 11. asset table (URL reference storage for files)
CREATE TABLE asset (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT NOT NULL REFERENCES content(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    url VARCHAR(1024) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 12. activity_log table (Operations auditing timeline log)
CREATE TABLE activity_log (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    brand_id BIGINT REFERENCES brand(id) ON DELETE SET NULL,
    content_id BIGINT REFERENCES content(id) ON DELETE SET NULL,
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE RESTRICT,
    action VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================================
-- DATABASE TRIGGER ATTACHMENTS
-- =========================================================================

CREATE TRIGGER trg_org_update BEFORE UPDATE ON organization FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_user_update BEFORE UPDATE ON "user" FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_brand_update BEFORE UPDATE ON brand FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_content_update BEFORE UPDATE ON content FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_assignment_update BEFORE UPDATE ON assignment FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_task_update BEFORE UPDATE ON task FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_comment_update BEFORE UPDATE ON comment FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_research_update BEFORE UPDATE ON research_item FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_script_update BEFORE UPDATE ON script FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_asset_update BEFORE UPDATE ON asset FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =========================================================================
-- PERFORMANCE & INTEGRITY INDEXING
-- =========================================================================

-- Indexing Foreign Keys to optimize joins & lookups
CREATE INDEX idx_user_organization_id ON "user"(organization_id);
CREATE INDEX idx_brand_organization_id ON brand(organization_id);
CREATE INDEX idx_content_brand_id ON content(brand_id);
CREATE INDEX idx_assignment_content_id ON assignment(content_id);
CREATE INDEX idx_assignment_user_id ON assignment(user_id);
CREATE INDEX idx_task_content_id ON task(content_id);
CREATE INDEX idx_comment_content_id ON comment(content_id);
CREATE INDEX idx_research_item_content_id ON research_item(content_id);
CREATE INDEX idx_script_content_id ON script(content_id);
CREATE INDEX idx_script_version_script_id ON script_version(script_id);
CREATE INDEX idx_asset_content_id ON asset(content_id);
CREATE INDEX idx_activity_log_organization_id ON activity_log(organization_id);
CREATE INDEX idx_activity_log_content_id ON activity_log(content_id);

-- Soft Delete Composite Indexing (optimized query filter paths)
CREATE INDEX idx_brand_org_deleted ON brand(organization_id, is_deleted);
CREATE INDEX idx_content_brand_deleted ON content(brand_id, is_deleted);

-- Kanban Board pipeline sorting optimization index
CREATE INDEX idx_content_stage_due ON content(brand_id, stage, due_date);
