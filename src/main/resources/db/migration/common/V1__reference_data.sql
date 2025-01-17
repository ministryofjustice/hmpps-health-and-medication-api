-- Reference Data Domain

CREATE TABLE reference_data_domain
(
    code                VARCHAR(40)              NOT NULL,
    parent_domain_code  VARCHAR(40),
    description         VARCHAR(100)             NOT NULL,
    list_sequence       INT DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by          VARCHAR(40)              NOT NULL,
    last_modified_at    TIMESTAMP WITH TIME ZONE,
    last_modified_by    VARCHAR(40),
    deactivated_at      TIMESTAMP WITH TIME ZONE,
    deactivated_by      VARCHAR(40),

    CONSTRAINT reference_data_domain_pk PRIMARY KEY (code),
    CONSTRAINT reference_data_domain_parent_fk FOREIGN KEY (parent_domain_code) REFERENCES reference_data_domain (code)
);

CREATE INDEX reference_data_domain_description_idx ON reference_data_domain (description);
CREATE INDEX reference_data_domain_list_sequence_idx ON reference_data_domain (list_sequence);
CREATE INDEX reference_data_domain_created_at_idx ON reference_data_domain (created_at);

COMMENT ON TABLE reference_data_domain IS 'Reference data domains for health and medication data';
COMMENT ON COLUMN reference_data_domain.list_sequence IS 'Used for ordering reference data correctly in lists. 0 is default order by description';
COMMENT ON COLUMN reference_data_domain.parent_domain_code IS 'Used for creating subdomains of other codes';

-- Reference Data Code

CREATE TABLE reference_data_code
(
    id               VARCHAR(81)              NOT NULL,
    domain           VARCHAR(40)              NOT NULL REFERENCES reference_data_domain (code),
    code             VARCHAR(40)              NOT NULL,
    description      VARCHAR(100)             NOT NULL,
    list_sequence    INT DEFAULT 0,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by       VARCHAR(40)              NOT NULL,
    last_modified_at TIMESTAMP WITH TIME ZONE,
    last_modified_by VARCHAR(40),
    deactivated_at   TIMESTAMP WITH TIME ZONE,
    deactivated_by   VARCHAR(40),

    CONSTRAINT reference_data_code_pk PRIMARY KEY (id),
    UNIQUE (code, domain)
);

CREATE INDEX reference_data_code_description_idx ON reference_data_code (description);
CREATE INDEX reference_data_code_list_sequence_idx ON reference_data_code (list_sequence);
CREATE INDEX reference_data_code_created_at_idx ON reference_data_code (created_at);

COMMENT ON TABLE reference_data_code IS 'Reference data codes for health and medication data';
COMMENT ON COLUMN reference_data_code.id IS 'Primary key, uses `domain`_`code`';
COMMENT ON COLUMN reference_data_code.list_sequence IS 'Used for ordering reference data correctly in lists and dropdowns. 0 is default order by description';
