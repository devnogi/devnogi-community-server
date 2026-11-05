ALTER TABLE user_summary
    ADD COLUMN last_auth_sync_revision BIGINT NULL;

CREATE TABLE user_summary_dlt_inbox (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dlt_topic VARCHAR(255) NOT NULL,
    dlt_partition INT NOT NULL,
    dlt_offset BIGINT NOT NULL,
    payload BLOB NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    next_attempt_at_ms BIGINT NOT NULL,
    claim_token VARCHAR(36) NULL,
    claim_until_ms BIGINT NULL,
    completed_at_ms BIGINT NULL,
    last_error VARCHAR(255) NULL,
    created_at_ms BIGINT NOT NULL,
    UNIQUE KEY uq_user_summary_dlt_record (dlt_topic, dlt_partition, dlt_offset),
    INDEX idx_user_summary_dlt_due (completed_at_ms, next_attempt_at_ms, claim_until_ms)
);
