CREATE TABLE outfit_post (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT NOT NULL,
 content TEXT NOT NULL,
 is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
 like_count BIGINT NOT NULL DEFAULT 0,
 comment_count BIGINT NOT NULL DEFAULT 0,
 share_count BIGINT NOT NULL DEFAULT 0,
 created_at DATETIME(6) NOT NULL,
 updated_at DATETIME(6) NOT NULL,
 INDEX idx_outfit_latest (is_deleted, created_at, id),
 INDEX idx_outfit_author (user_id, is_deleted, created_at, id)
);
CREATE TABLE outfit_image (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 outfit_id BIGINT NOT NULL,
 stored_name VARCHAR(255) NOT NULL,
 mime_type VARCHAR(50) NOT NULL,
 byte_size BIGINT NOT NULL,
 display_order INT NOT NULL,
 CONSTRAINT fk_outfit_image_post FOREIGN KEY (outfit_id) REFERENCES outfit_post(id),
 INDEX idx_outfit_image_order (outfit_id, display_order)
);
CREATE TABLE outfit_item (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 outfit_id BIGINT NOT NULL,
 slot VARCHAR(20) NOT NULL,
 input_type VARCHAR(10) NOT NULL,
 name VARCHAR(255) NOT NULL,
 top_category VARCHAR(50),
 sub_category VARCHAR(50),
 normalized_name VARCHAR(255) NOT NULL,
 CONSTRAINT fk_outfit_item_post FOREIGN KEY (outfit_id) REFERENCES outfit_post(id),
 CONSTRAINT uq_outfit_slot UNIQUE (outfit_id, slot),
 INDEX idx_outfit_item_search (name, slot, outfit_id),
 INDEX idx_outfit_motion (slot, input_type, normalized_name, outfit_id)
);
CREATE TABLE outfit_tag (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 outfit_id BIGINT NOT NULL,
 tag VARCHAR(80) NOT NULL,
 normalized_tag VARCHAR(80) NOT NULL,
 CONSTRAINT fk_outfit_tag_post FOREIGN KEY (outfit_id) REFERENCES outfit_post(id),
 CONSTRAINT uq_outfit_tag UNIQUE (outfit_id, normalized_tag),
 INDEX idx_outfit_tag_search (normalized_tag, outfit_id)
);
CREATE TABLE outfit_like (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 outfit_id BIGINT NOT NULL,
 user_id BIGINT NOT NULL,
 created_at DATETIME(6) NOT NULL,
 CONSTRAINT fk_outfit_like_post FOREIGN KEY (outfit_id) REFERENCES outfit_post(id),
 CONSTRAINT uq_outfit_like UNIQUE (outfit_id, user_id)
);
CREATE TABLE outfit_comment (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 outfit_id BIGINT NOT NULL,
 user_id BIGINT NOT NULL,
 parent_id BIGINT,
 content TEXT NOT NULL,
 is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
 created_at DATETIME(6) NOT NULL,
 updated_at DATETIME(6) NOT NULL,
 CONSTRAINT fk_outfit_comment_post FOREIGN KEY (outfit_id) REFERENCES outfit_post(id),
 CONSTRAINT fk_outfit_comment_parent FOREIGN KEY (parent_id) REFERENCES outfit_comment(id),
 INDEX idx_outfit_comment_thread (outfit_id, parent_id, created_at, id)
);
CREATE TABLE outfit_share (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 outfit_id BIGINT NOT NULL,
 user_id BIGINT NOT NULL,
 request_id VARCHAR(36) NOT NULL,
 channel VARCHAR(10) NOT NULL,
 created_at DATETIME(6) NOT NULL,
 CONSTRAINT fk_outfit_share_post FOREIGN KEY (outfit_id) REFERENCES outfit_post(id),
 CONSTRAINT uq_outfit_share_request UNIQUE (outfit_id, user_id, request_id)
);
CREATE TABLE outfit_bookmark (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 outfit_id BIGINT NOT NULL,
 user_id BIGINT NOT NULL,
 created_at DATETIME(6) NOT NULL,
 CONSTRAINT fk_outfit_bookmark_post FOREIGN KEY (outfit_id) REFERENCES outfit_post(id),
 CONSTRAINT uq_outfit_bookmark UNIQUE (outfit_id, user_id),
 INDEX idx_outfit_saved (user_id, created_at, outfit_id)
);
