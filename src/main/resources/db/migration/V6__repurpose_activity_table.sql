DROP TABLE IF EXISTS activity_log CASCADE;
DROP TABLE IF EXISTS activity CASCADE;

CREATE TABLE activity (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT NOT NULL REFERENCES content(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE RESTRICT,
    event_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    metadata_json TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index foreign keys for performance
CREATE INDEX idx_activity_content_id ON activity(content_id);
CREATE INDEX idx_activity_user_id ON activity(user_id);
