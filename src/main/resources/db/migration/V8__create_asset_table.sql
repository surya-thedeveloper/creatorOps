CREATE TABLE asset (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT NOT NULL REFERENCES content(id) ON DELETE CASCADE,
    uploaded_by_user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE RESTRICT,
    asset_type VARCHAR(50) NOT NULL,
    asset_source VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    file_url VARCHAR(2048) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index foreign keys for performance
CREATE INDEX idx_asset_content_id ON asset(content_id);
CREATE INDEX idx_asset_uploaded_by_user_id ON asset(uploaded_by_user_id);

-- Enforce automatic updated_at timestamp updating
CREATE TRIGGER trg_asset_update BEFORE UPDATE ON asset FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
