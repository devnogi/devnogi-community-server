CREATE TABLE post_meta(
    post_id       BIGINT NOT NULL,
    view_count    INT    NOT NULL DEFAULT 0,
    like_count    INT    NOT NULL DEFAULT 0,
    comment_count INT    NOT NULL DEFAULT 0,
    PRIMARY KEY (post_id),
    CONSTRAINT fk_post_meta_post
        FOREIGN KEY (post_id)
            REFERENCES post (id)
            ON DELETE CASCADE
);

ALTER TABLE `post`
DROP COLUMN `like_count`,
DROP COLUMN `view_count`,
DROP COLUMN `comment_count`;