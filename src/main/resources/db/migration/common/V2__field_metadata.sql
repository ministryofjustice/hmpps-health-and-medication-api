CREATE TABLE field_metadata
(
    prisoner_number             VARCHAR(7)                  NOT NULL,
    field                       VARCHAR(40),
    last_modified_at            TIMESTAMP WITH TIME ZONE    NOT NULL,
    last_modified_by            VARCHAR(40)                 NOT NULL,

    CONSTRAINT field_metadata_pk PRIMARY KEY (prisoner_number, field)
);

COMMENT ON TABLE field_metadata IS 'Field level metadata such as when a field was modified and who by';
COMMENT ON COLUMN field_metadata.prisoner_number IS 'First part of the primary key - the identifier of a prisoner (also often called prison number, NOMS number, offender number...)';
COMMENT ON COLUMN field_metadata.field IS 'Second part of the primary key - the field name that the metadata is linked to';
COMMENT ON COLUMN field_metadata.last_modified_at IS 'Timestamp of last modification';
COMMENT ON COLUMN field_metadata.last_modified_by IS 'The username of the user modifying the record';
