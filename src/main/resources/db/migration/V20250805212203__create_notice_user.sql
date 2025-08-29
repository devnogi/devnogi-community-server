CREATE TABLE notice_user
(
    id        BIGINT AUTO_INCREMENT NOT NULL,
    notice_id BIGINT NOT NULL,
    user_id   BIGINT NOT NULL,
    is_read   TINYINT(1) NOT NULL,
    CONSTRAINT pk_notice_user PRIMARY KEY (id)
);

ALTER TABLE notice
DROP COLUMN user_id;


ALTER TABLE notice
DROP COLUMN is_read;