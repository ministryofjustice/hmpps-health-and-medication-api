DELETE FROM catering_instructions WHERE instructions IS NULL;

ALTER TABLE catering_instructions ALTER COLUMN instructions SET NOT NULL;