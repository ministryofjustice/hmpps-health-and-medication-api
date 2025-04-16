DELETE FROM field_history;
DELETE FROM field_metadata;
DELETE FROM health;
DELETE FROM food_allergy;
DELETE FROM medical_dietary_requirement;
DELETE FROM personalised_dietary_requirement;
DELETE FROM catering_instructions;

ALTER SEQUENCE field_history_field_history_id_seq RESTART
