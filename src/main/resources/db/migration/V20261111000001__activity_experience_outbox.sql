ALTER TABLE user_summary ADD COLUMN activity_grade VARCHAR(32) NULL;

CREATE TABLE activity_outbox (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_id CHAR(36) NOT NULL,
    activity_key VARCHAR(160) NOT NULL,
    activity_type VARCHAR(32) NOT NULL,
    actor_user_id BIGINT NOT NULL,
    author_user_id BIGINT NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    target_id BIGINT NOT NULL,
    root_target_type VARCHAR(32) NOT NULL,
    root_target_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL,
    baseline BOOLEAN NOT NULL DEFAULT FALSE,
    occurred_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    published_at DATETIME(6) NULL,
    claim_token CHAR(36) NULL,
    claimed_until DATETIME(6) NULL,
    next_attempt_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    attempts INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_activity_event (event_id),
    KEY ix_activity_pending (published_at, next_attempt_at, claimed_until, id),
    KEY ix_activity_root (root_target_type, root_target_id, id)
);

CREATE TABLE activity_backfill_checkpoint (
    source VARCHAR(32) NOT NULL PRIMARY KEY,
    cutoff_id BIGINT NOT NULL,
    last_id BIGINT NOT NULL DEFAULT 0,
    cutoff_time DATETIME(6) NOT NULL,
    complete BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);
