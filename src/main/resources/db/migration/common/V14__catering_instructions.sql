CREATE TABLE catering_instructions
(
    prisoner_number VARCHAR(7) NOT NULL,
    instructions    VARCHAR(1000),

    CONSTRAINT catering_instructions_pk PRIMARY KEY (prisoner_number)
);

COMMENT ON TABLE catering_instructions IS 'The catering instructions for a prisoner';
COMMENT ON COLUMN catering_instructions.prisoner_number IS 'Primary key - the identifier of a prisoner (also often called prison number, NOMS number, offender number...)';
COMMENT ON COLUMN catering_instructions.instructions IS 'Optional free text specifying any specific catering instructions for the prisoner';
