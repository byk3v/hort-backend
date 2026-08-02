SET search_path TO hort;

-- Existing non-production checkout timestamps are interpreted as UTC.
ALTER TABLE check_out
  ALTER COLUMN occurred_at TYPE TIMESTAMP WITH TIME ZONE
    USING occurred_at AT TIME ZONE 'UTC';

ALTER TABLE check_out ADD CONSTRAINT uk_check_out_id_hort UNIQUE (id, hort_id);

CREATE TABLE attendance_session (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  hort_id UUID NOT NULL REFERENCES hort(id) ON DELETE RESTRICT,
  student_id UUID NOT NULL,
  attendance_date DATE NOT NULL,
  checked_in_at TIMESTAMP WITH TIME ZONE NOT NULL,
  checked_in_by_user_id VARCHAR(255) NOT NULL,
  check_in_comment VARCHAR(500),
  checked_out_at TIMESTAMP WITH TIME ZONE,
  checked_out_by_user_id VARCHAR(255),
  check_out_id UUID,
  created_by VARCHAR(64) NOT NULL DEFAULT 'system',
  created_date TIMESTAMP NOT NULL DEFAULT NOW(),
  last_modified_by VARCHAR(64),
  last_modified_date TIMESTAMP,
  CONSTRAINT uk_attendance_student_day UNIQUE (hort_id, student_id, attendance_date),
  CONSTRAINT uk_attendance_checkout UNIQUE (check_out_id),
  CONSTRAINT ck_attendance_checkout_complete CHECK (
    (checked_out_at IS NULL AND checked_out_by_user_id IS NULL AND check_out_id IS NULL)
    OR
    (checked_out_at IS NOT NULL AND checked_out_by_user_id IS NOT NULL AND check_out_id IS NOT NULL)
  ),
  CONSTRAINT fk_attendance_student_hort
    FOREIGN KEY (student_id, hort_id) REFERENCES student(id, hort_id),
  CONSTRAINT fk_attendance_checkout_hort
    FOREIGN KEY (check_out_id, hort_id) REFERENCES check_out(id, hort_id)
);

CREATE INDEX idx_attendance_present
  ON attendance_session(hort_id, attendance_date, student_id)
  WHERE checked_out_at IS NULL;

ALTER TABLE attendance_session ENABLE ROW LEVEL SECURITY;
ALTER TABLE attendance_session FORCE ROW LEVEL SECURITY;

CREATE POLICY p_attendance_session_by_hort ON attendance_session
  USING (hort_id = NULLIF(current_setting('app.hort_id', true), '')::uuid)
  WITH CHECK (hort_id = NULLIF(current_setting('app.hort_id', true), '')::uuid);

DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'hort_app') THEN
    GRANT SELECT, INSERT, UPDATE, DELETE ON attendance_session TO hort_app;
  END IF;
END $$;
