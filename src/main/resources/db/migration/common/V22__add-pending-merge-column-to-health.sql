-- add pending merge column to health table
ALTER TABLE health ADD COLUMN pending_merge_to_prisoner_number VARCHAR(7);

-- done as partial index since we expect this to mostly contain nulls
CREATE INDEX health_pending_merge_to_prisoner_number_idx
    ON health (pending_merge_to_prisoner_number)
    WHERE pending_merge_to_prisoner_number IS NOT NULL;