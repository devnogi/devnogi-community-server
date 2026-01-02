DROP TABLE post_image;


CREATE TABLE post_image
(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    original_file_name VARCHAR(255) NOT NULL COMMENT '사용자 업로드 원본명',
    stored_file_name VARCHAR(255) NOT NULL COMMENT 'MinIO 저장용 UUID명',
    CONSTRAINT fk_post_image_post FOREIGN KEY (post_id)
    REFERENCES post(id)
    ON DELETE CASCADE,
    INDEX idx_post_id (post_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;