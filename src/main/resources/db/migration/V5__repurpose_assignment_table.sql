DROP TABLE IF EXISTS assignment CASCADE;

CREATE TABLE assignment (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT NOT NULL REFERENCES content(id) ON DELETE CASCADE,
    assigned_to_user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE RESTRICT,
    assigned_by_user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE RESTRICT,
    assignment_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ASSIGNED',
    notes TEXT,
    due_date TIMESTAMPTZ,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index foreign keys for join performance
CREATE INDEX idx_assignment_content_id ON assignment(content_id);
CREATE INDEX idx_assignment_assigned_to_user_id ON assignment(assigned_to_user_id);
CREATE INDEX idx_assignment_assigned_by_user_id ON assignment(assigned_by_user_id);

-- Enforce automatic updated_at timestamp updating
CREATE TRIGGER trg_assignment_update BEFORE UPDATE ON assignment FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
