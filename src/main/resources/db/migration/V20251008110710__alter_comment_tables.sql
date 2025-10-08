ALTER TABLE comment
    MODIFY post_id BIGINT NULL;


ALTER TABLE comment_archive
    MODIFY post_id BIGINT NULL;