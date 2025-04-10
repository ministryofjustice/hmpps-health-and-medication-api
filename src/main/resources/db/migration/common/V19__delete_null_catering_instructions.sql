DELETE FROM catering_instructions WHERE instructions IS NULL OR trim(instructions) = '';

ALTER TABLE catering_instructions ALTER COLUMN instructions SET NOT NULL;