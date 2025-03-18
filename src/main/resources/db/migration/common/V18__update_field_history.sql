ALTER TABLE field_history ADD COLUMN prison_id VARCHAR(3);

UPDATE field_history SET prison_id = 'STI' WHERE prison_id IS NULL;

ALTER TABLE field_history ALTER COLUMN prison_id SET NOT NULL;
