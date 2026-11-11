-- Existing published rows retain their historical creation provenance; new drafts are initialized
-- only when first published. Editing back to a draft never resets this marker or earned XP.
ALTER TABLE post ADD COLUMN first_published_at DATETIME(6) NULL;
UPDATE post SET first_published_at=created_at WHERE is_draft=0;
