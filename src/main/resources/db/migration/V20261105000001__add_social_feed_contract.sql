ALTER TABLE post
    MODIFY board_id BIGINT NULL,
    ADD COLUMN plain_content TEXT NULL AFTER content,
    ADD COLUMN content_format VARCHAR(32) NOT NULL DEFAULT 'LEGACY_MARKDOWN' AFTER plain_content;

ALTER TABLE post_archive
    MODIFY board_id BIGINT NULL,
    ADD COLUMN plain_content TEXT NULL AFTER content,
    ADD COLUMN content_format VARCHAR(32) NOT NULL DEFAULT 'LEGACY_MARKDOWN' AFTER plain_content;

ALTER TABLE post_image
    ADD COLUMN media_type VARCHAR(16) NOT NULL DEFAULT 'IMAGE' AFTER stored_file_name,
    ADD COLUMN mime_type VARCHAR(100) NOT NULL DEFAULT 'image/jpeg' AFTER media_type,
    ADD COLUMN file_size BIGINT NOT NULL DEFAULT 0 AFTER mime_type,
    ADD COLUMN display_order INT NOT NULL DEFAULT 0 AFTER file_size;

ALTER TABLE announcement MODIFY board_id BIGINT NULL;

ALTER TABLE user_summary
    ADD COLUMN is_verified BOOLEAN NOT NULL DEFAULT FALSE AFTER server_name;

-- Auth only assigns server_name to successfully verified game identities and
-- clears it when ownership moves, so this safely preserves existing badges.
UPDATE user_summary
SET is_verified = TRUE
WHERE server_name IS NOT NULL
  AND TRIM(server_name) <> '';

CREATE TABLE post_bookmark
(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_post_bookmark_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT uk_post_bookmark_user_post UNIQUE (user_id, post_id),
    INDEX idx_post_bookmark_user_created (user_id, created_at DESC, id DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
