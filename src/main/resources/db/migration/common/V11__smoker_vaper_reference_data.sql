INSERT INTO reference_data_domain (code, description, list_sequence, created_at, created_by)
VALUES ('SMOKER', 'Smoker or vaper', 0, '2025-02-07 00:00:00+0000', 'CONNECT_DPS');

INSERT INTO reference_data_code (id, domain, code, description, list_sequence, created_at, created_by)
VALUES ('SMOKER_NO', 'SMOKER', 'NO', 'No, they do not smoke or vape', 0, '2025-02-07 00:00:00+0000', 'CONNECT_DPS'),
       ('SMOKER_YES', 'SMOKER', 'YES', 'Yes, they smoke', 1, '2025-02-07 00:00:00+0000', 'CONNECT_DPS'),
       ('SMOKER_VAPER', 'SMOKER', 'VAPER', 'Yes, they vape or use nicotine replacement therapy (NRT)', 2,
        '2025-02-07 00:00:00+0000', 'CONNECT_DPS');

