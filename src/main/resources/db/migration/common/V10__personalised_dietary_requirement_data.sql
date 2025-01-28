INSERT INTO reference_data_domain (code, description, list_sequence, created_at, created_by)
VALUES ('PERSONALISED_DIET', 'Personalised diet', 0, '2025-01-16 00:00:00+0000', 'CONNECT_DPS');

INSERT INTO reference_data_code (id, domain, code, description, list_sequence, created_at, created_by)
VALUES
('PERSONALISED_DIET_KOSHER', 'PERSONALISED_DIET', 'KOSHER', 'Kosher', 0, '2025-01-16 00:00:00+0000', 'CONNECT_DPS'),
('PERSONALISED_DIET_VEGAN', 'PERSONALISED_DIET', 'VEGAN', 'Vegan', 1, '2025-01-16 00:00:00+0000', 'CONNECT_DPS'),
('PERSONALISED_DIET_OTHER', 'PERSONALISED_DIET', 'OTHER', 'Other', 2, '2025-01-16 00:00:00+0000', 'CONNECT_DPS');

