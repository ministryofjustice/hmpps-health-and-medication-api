CREATE TABLE medical_dietary_requirement
(
    id                  BIGSERIAL   NOT NULL,
    prisoner_number     VARCHAR(7)  NOT NULL,
    dietary_requirement VARCHAR(81) NOT NULL,
    comment_text        VARCHAR(255),
    CONSTRAINT medical_dietary_requirement_pk PRIMARY KEY (id),
    CONSTRAINT dietary_requirement_fk FOREIGN KEY (dietary_requirement) REFERENCES reference_data_code (id)
);

CREATE INDEX medical_dietary_requirement_prisoner_number_idx ON medical_dietary_requirement (prisoner_number);

COMMENT ON TABLE medical_dietary_requirement IS 'The list of medical dietary requirements the prisoner has';
COMMENT ON COLUMN medical_dietary_requirement.id IS 'The primary key, in case prisoners require multiple "other" values';
COMMENT ON COLUMN medical_dietary_requirement.prisoner_number IS 'The identifier of a prisoner (also often called prison number, NOMS number, offender number...)';
COMMENT ON COLUMN medical_dietary_requirement.dietary_requirement IS 'The medical dietary requirement relevant to a prisoner (the prisoner can have more than one)';
COMMENT ON COLUMN medical_dietary_requirement.comment_text IS 'Optional user supplied text, for example to provide details of an "OTHER" selection';
