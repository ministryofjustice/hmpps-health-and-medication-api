ALTER TABLE field_metadata ADD COLUMN last_modified_prison_id VARCHAR(3);

UPDATE field_metadata SET last_modified_prison_id = 'STI' WHERE last_modified_prison_id IS NULL;

ALTER TABLE field_metadata ALTER COLUMN last_modified_prison_id SET NOT NULL;
