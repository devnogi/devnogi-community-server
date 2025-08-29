CREATE TABLE notice_user
(
    id        BIGINT AUTO_INCREMENT NOT NULL,
    notice_id BIGINT NOT NULL,
    user_id   BIGINT NOT NULL,
    is_read   TINYINT(1) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_notice_user PRIMARY KEY (id),
    KEY idx_notice_user_notice_id (notice_id),
    KEY idx_notice_user_user_id (user_id)
);

ALTER TABLE notice
DROP COLUMN user_id;

ALTER TABLE notice
DROP COLUMN is_read;