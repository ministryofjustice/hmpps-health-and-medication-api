INSERT INTO field_history (field_history_id, prisoner_number, field, value_ref, value_string, value_json, created_at, created_by, prison_id)
VALUES (-201, 'A1234AA', 'FOOD_ALLERGY', null, null,
        '{ "field": "FOOD_ALLERGY", "value": { "allergies": [{ "value": "FOOD_ALLERGY_SOYA" }]}}',
        '2024-01-02 09:10:11.123', 'USER1', 'STI'),
       (-202, 'A1234AA', 'MEDICAL_DIET', null, null,
        '{ "field": "MEDICAL_DIET", "value": { "medicalDietaryRequirements": [{ "value": "MEDICAL_DIET_LOW_CHOLESTEROL" }]}}',
        '2024-01-02 09:10:11.123', 'USER1', 'STI'),
       (-203, 'A1234AA', 'PERSONALISED_DIET', null, null,
        '{ "field": "PERSONALISED_DIET", "value": { "personalisedDietaryRequirements": [{ "value": "PERSONALISED_DIET_KOSHER" }]}}',
        '2024-01-02 09:10:11.123', 'USER1', 'STI'),
       (-204, 'A1234AA', 'CATERING_INSTRUCTIONS', null, 'Serve dessert before the main course.',
        null, '2024-01-02 09:10:11.123', 'USER1', 'STI');
