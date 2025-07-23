CREATE TABLE comment_meta
(
    comment_id BIGINT NOT NULL,
    like_count INT    NOT NULL DEFAULT 0,
    PRIMARY KEY (comment_id),
    CONSTRAINT fk_comment_meta_comment
        FOREIGN KEY (comment_id)
            REFERENCES comment (id)
            ON DELETE CASCADE
);

ALTER TABLE `comment`
DROP
COLUMN `like_count`;

