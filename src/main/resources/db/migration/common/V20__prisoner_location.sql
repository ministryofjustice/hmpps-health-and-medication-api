CREATE TABLE prisoner_location
(
    prisoner_number      VARCHAR(7)    NOT NULL,
    prison_id            VARCHAR(3),
    l1_location          VARCHAR(12),
    location             VARCHAR(51),
    last_admission_date  DATE,

    CONSTRAINT prisoner_location_pk PRIMARY KEY (prisoner_number)
);

CREATE INDEX prisoner_location_prison_id_idx ON prisoner_location (prison_id);
CREATE INDEX prisoner_location_l1_location_idx ON prisoner_location (prison_id, l1_location);

COMMENT ON TABLE prisoner_location IS 'The location related information for a prisoner';
COMMENT ON COLUMN prisoner_location.prisoner_number IS 'The identifier of a prisoner (also often called prison number, NOMS number, offender number...)';
COMMENT ON COLUMN prisoner_location.prison_id IS 'The identifier for the prison that the prisoner is currently in';
COMMENT ON COLUMN prisoner_location.l1_location IS 'The level 1 (top level) location code from NOMIS. This is the highest level of the prisoner location hierarchy';
COMMENT ON COLUMN prisoner_location.location IS 'The full location of the prisoner within the prison';
COMMENT ON COLUMN prisoner_location.last_admission_date IS 'The most recent prisoner admission date';