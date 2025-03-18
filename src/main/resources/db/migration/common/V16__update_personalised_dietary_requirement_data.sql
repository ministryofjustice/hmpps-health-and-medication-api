
INSERT INTO reference_data_code (id, domain, code, description, list_sequence, created_at, created_by)
VALUES
('PERSONALISED_DIET_HALAL', 'PERSONALISED_DIET', 'HALAL', 'Halal', 0, '2025-03-17 00:00:00+0000', 'CONNECT_DPS');

UPDATE reference_data_code SET list_sequence = 1 WHERE id = 'PERSONALISED_DIET_KOSHER';
UPDATE reference_data_code SET list_sequence = 2 WHERE id = 'PERSONALISED_DIET_VEGAN';
UPDATE reference_data_code SET list_sequence = 3 WHERE id = 'PERSONALISED_DIET_OTHER';
