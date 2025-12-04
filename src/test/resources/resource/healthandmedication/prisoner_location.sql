INSERT INTO prisoner_location (prisoner_number, prison_id, l1_location, l1_description, location, last_admission_date)
VALUES ('A1234AA', 'STI', 'A', 'A Wing', 'A-1-001', '2025-11-21'),
       ('B1234AA', 'STI', 'A', 'A Wing', 'A-1-002-A', '2025-11-22'),
       ('B1234BB', 'STI', 'B', 'B Wing', 'B-3-014', '2025-11-07'),
--      Temporary location
       ('B1234CC', 'STI', 'RECP', 'Reception', null, '2025-11-04'),
--      No cell location info
       ('B1234DD', 'STI', null, null, null, null),
--      Different establishment
       ('Z1234ZZ', 'KMI', 'E', 'Block E', 'E-A5-001', null);