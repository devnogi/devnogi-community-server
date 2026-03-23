ALTER TABLE user_summary
    ADD server_name VARCHAR(50) NOT NULL;

ALTER TABLE user_summary
    DROP INDEX uq_user_summnary_nickname;

ALTER TABLE user_summary
    ADD CONSTRAINT uq_user_summary_server_nickname
        UNIQUE (server_name, nickname);