CREATE TABLE field_history
(
    field_history_id BIGSERIAL                NOT NULL,
    prisoner_number  VARCHAR(7)               NOT NULL,
    field            VARCHAR(40),
    value_int        INT,
    value_string     VARCHAR(40),
    value_ref        VARCHAR(81),
    value_json       TEXT,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by       VARCHAR(40)              NOT NULL,
    merged_at        TIMESTAMP WITH TIME ZONE,
    merged_from      VARCHAR(7),

    CONSTRAINT field_history_pk PRIMARY KEY (field_history_id),
    CONSTRAINT value_ref_reference_data_code_fk FOREIGN KEY (value_ref) REFERENCES reference_data_code (id),
    CONSTRAINT field_history_one_value_only_ck CHECK (
        (value_int IS NULL AND value_string IS NULL AND value_ref IS NULL) OR
        (value_int IS NOT NULL AND value_string IS NULL AND value_ref IS NULL) OR
        (value_int IS NULL AND value_string IS NOT NULL AND value_ref IS NULL) OR
        (value_int IS NULL AND value_string IS NULL AND value_ref IS NOT NULL)
        )
);

CREATE INDEX field_history_prisoner_number_field_idx ON field_history (prisoner_number, field);

COMMENT ON TABLE field_history IS 'The field level history of prisoner health and medication data';
COMMENT ON COLUMN field_history.prisoner_number IS 'The identifier of a prisoner (also often called prison number, NOMS number, offender number...)';
COMMENT ON COLUMN field_history.field IS 'The field that this history record is for';
COMMENT ON COLUMN field_history.value_int IS 'The integer value for the field if the field represents an integer';
COMMENT ON COLUMN field_history.value_string IS 'The string value for the field if the field represents a string';
COMMENT ON COLUMN field_history.value_ref IS 'The reference_data_code.id for the field if the field represents a foreign key to reference_data_code';
COMMENT ON COLUMN field_history.value_json IS 'Used for storing generic json data in text form';
COMMENT ON COLUMN field_history.created_at IS 'Timestamp of when the history record was created';
COMMENT ON COLUMN field_history.created_by IS 'The username of the user creating the history record';
COMMENT ON COLUMN field_history.merged_at IS 'Timestamp of when the history record was merged from another prisoner number';
COMMENT ON COLUMN field_history.merged_from IS 'The old prisoner number that this history item was merged from';
