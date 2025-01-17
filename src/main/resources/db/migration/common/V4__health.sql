CREATE TABLE health
(
    prisoner_number  VARCHAR(7) NOT NULL,
    smoker_or_vaper VARCHAR(81),

    CONSTRAINT health_pk PRIMARY KEY (prisoner_number),
    CONSTRAINT smoker_reference_data_code_fk FOREIGN KEY (smoker_or_vaper) REFERENCES reference_data_code (id)
);

COMMENT ON TABLE health IS 'The health related information of a prisoner';
COMMENT ON COLUMN health.prisoner_number IS 'Primary key - the identifier of a prisoner (also often called prison number, NOMS number, offender number...)';
COMMENT ON COLUMN health.smoker_or_vaper IS 'Whether the prisoner is a smoker or vaper, from reference data domain SMOKE';
