ALTER TABLE outfit_post ADD COLUMN gender VARCHAR(6) NULL;
ALTER TABLE outfit_post ADD CONSTRAINT chk_outfit_gender CHECK (gender IN ('MALE', 'FEMALE'));
CREATE INDEX idx_outfit_gender_latest ON outfit_post (is_deleted, gender, created_at, id);
