-- Add soft delete columns to health table
ALTER TABLE health ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE health ADD COLUMN deleted_by VARCHAR(100);
ALTER TABLE health ADD COLUMN deletion_reason TEXT;

-- done as partial index since we expect this to mostly contain nulls
CREATE INDEX health_deleted_at_idx
  ON health (deleted_at)
  WHERE deleted_at IS NOT NULL;
