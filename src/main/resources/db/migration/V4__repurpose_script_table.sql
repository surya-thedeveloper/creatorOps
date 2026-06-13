-- Drop legacy tables to implement direct 1:N Content to Script versions structure
DROP TABLE IF EXISTS script_version CASCADE;
DROP TABLE IF EXISTS script CASCADE;

CREATE TABLE script (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT NOT NULL REFERENCES content(id) ON DELETE CASCADE,
    version INT NOT NULL,
    generated_script TEXT,
    editor_content TEXT,
    document_type VARCHAR(50) NOT NULL,
    external_document_url VARCHAR(1024),
    uploaded_file_reference VARCHAR(1024),
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index foreign keys for join performance
CREATE INDEX idx_script_content_id ON script(content_id);
CREATE INDEX idx_script_user_id ON script(user_id);

-- Enforce automatic updated_at timestamp updating
CREATE TRIGGER trg_script_update BEFORE UPDATE ON script FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
