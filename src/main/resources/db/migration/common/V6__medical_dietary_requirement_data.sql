INSERT INTO reference_data_domain (code, description, list_sequence, created_at, created_by)
VALUES ('MEDICAL_DIET', 'Medical diet', 0, '2025-01-16 00:00:00+0000', 'CONNECT_DPS');

INSERT INTO reference_data_code (id, domain, code, description, list_sequence, created_at, created_by)
VALUES ('MEDICAL_DIET_COELIAC', 'MEDICAL_DIET', 'COELIAC', 'Coeliac (cannot eat gluten)', 0, '2025-01-16 00:00:00+0000',
        'CONNECT_DPS'),
       ('MEDICAL_DIET_DIABETIC_TYPE_1', 'MEDICAL_DIET', 'DIABETIC_TYPE_1', 'Diabetic type 1', 1,
        '2025-01-16 00:00:00+0000', 'CONNECT_DPS'),
       ('MEDICAL_DIET_DIABETIC_TYPE_2', 'MEDICAL_DIET', 'DIABETIC_TYPE_2', 'Diabetic type 2', 2,
        '2025-01-16 00:00:00+0000', 'CONNECT_DPS'),
       ('MEDICAL_DIET_DYSPHAGIA', 'MEDICAL_DIET', 'DYSPHAGIA', 'Dysphagia (has problems swallowing food)', 3,
        '2025-01-16 00:00:00+0000', 'CONNECT_DPS'),
       ('MEDICAL_DIET_EATING_DISORDER', 'MEDICAL_DIET', 'EATING_DISORDER', 'Eating disorder', 4,
        '2025-01-16 00:00:00+0000', 'CONNECT_DPS'),
       ('MEDICAL_DIET_LACTOSE_INTOLERANT', 'MEDICAL_DIET', 'LACTOSE_INTOLERANT', 'Lactose intolerant', 5,
        '2025-01-16 00:00:00+0000', 'CONNECT_DPS'),
       ('MEDICAL_DIET_LOW_CHOLESTEROL', 'MEDICAL_DIET', 'LOW_CHOLESTEROL', 'Low cholesterol', 6,
        '2025-01-16 00:00:00+0000', 'CONNECT_DPS'),
       ('MEDICAL_DIET_LOW_PHOSPHOROUS', 'MEDICAL_DIET', 'LOW_PHOSPHOROUS', 'Low phosphorous diet', 7,
        '2025-01-16 00:00:00+0000', 'CONNECT_DPS'),
       ('MEDICAL_DIET_NUTRIENT_DEFICIENCY', 'MEDICAL_DIET', 'NUTRIENT_DEFICIENCY', 'Nutrient deficiency', 8,
        '2025-01-16 00:00:00+0000', 'CONNECT_DPS'),
       ('MEDICAL_DIET_OTHER', 'MEDICAL_DIET', 'OTHER', 'Other', 9, '2025-01-16 00:00:00+0000', 'CONNECT_DPS');

