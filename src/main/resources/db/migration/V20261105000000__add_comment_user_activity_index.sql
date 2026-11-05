CREATE INDEX idx_comment_user_activity
    ON comment (user_id, is_deleted, is_blocked, created_at, id);
