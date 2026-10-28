CREATE INDEX idx_notice_user_notice_id_user_id
    ON notice_user (notice_id, user_id);

CREATE INDEX idx_notice_user_user_created
    ON notice_user (user_id, created_at DESC);

CREATE INDEX idx_user_comment ON comment_like (user_id, comment_id);
