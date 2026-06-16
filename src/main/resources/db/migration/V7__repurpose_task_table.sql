DROP TABLE IF EXISTS task CASCADE;

CREATE TABLE task (
    id BIGSERIAL PRIMARY KEY,
    assignment_id BIGINT NOT NULL REFERENCES assignment(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    assigned_to_user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE RESTRICT,
    created_by_user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE RESTRICT,
    due_date TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index foreign keys for performance
CREATE INDEX idx_task_assignment_id ON task(assignment_id);
CREATE INDEX idx_task_assigned_to_user_id ON task(assigned_to_user_id);
CREATE INDEX idx_task_created_by_user_id ON task(created_by_user_id);

-- Enforce automatic updated_at timestamp updating
CREATE TRIGGER trg_task_update BEFORE UPDATE ON task FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
