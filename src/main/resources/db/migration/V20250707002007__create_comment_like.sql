DROP TABLE IF EXISTS comment_like;

CREATE TABLE comment_like
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '좋아요 ID',
    comment_id BIGINT NOT NULL COMMENT '댓글 ID',
    user_id    BIGINT NOT NULL COMMENT '사용자 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '좋아요 일시',
    UNIQUE KEY uniq_comment_user (comment_id, user_id),
    INDEX      idx_comment_id (comment_id),
    INDEX      idx_user_id (user_id)
) COMMENT='댓글 좋아요';