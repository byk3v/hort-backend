SET search_path TO hort;

-- self_dismissal is tenant-owned like the student it references. Keeping the
-- tenant directly on the row makes policies and future composite constraints
-- explicit.
ALTER TABLE self_dismissal ADD COLUMN IF NOT EXISTS hort_id UUID;

UPDATE self_dismissal sd
SET hort_id = s.hort_id
FROM student s
WHERE sd.student_id = s.id
  AND sd.hort_id IS NULL;

ALTER TABLE self_dismissal ALTER COLUMN hort_id SET NOT NULL;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_self_dismissal_hort'
  ) THEN
    ALTER TABLE self_dismissal
      ADD CONSTRAINT fk_self_dismissal_hort
      FOREIGN KEY (hort_id) REFERENCES hort(id) ON DELETE RESTRICT;
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_self_dismissal_hort ON self_dismissal(hort_id);

ALTER TABLE self_dismissal ENABLE ROW LEVEL SECURITY;
ALTER TABLE self_dismissal FORCE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS p_self_dismissal_by_hort ON self_dismissal;
CREATE POLICY p_self_dismissal_by_hort ON self_dismissal
  USING (hort_id = NULLIF(current_setting('app.hort_id', true), '')::uuid)
  WITH CHECK (hort_id = NULLIF(current_setting('app.hort_id', true), '')::uuid);

-- FORCE also protects against accidentally running the application as the
-- table owner. ADR-003 still requires a dedicated non-owner runtime role.
ALTER TABLE person FORCE ROW LEVEL SECURITY;
ALTER TABLE hort_group FORCE ROW LEVEL SECURITY;
ALTER TABLE student FORCE ROW LEVEL SECURITY;
ALTER TABLE collector FORCE ROW LEVEL SECURITY;
ALTER TABLE pickup_right FORCE ROW LEVEL SECURITY;
ALTER TABLE check_out FORCE ROW LEVEL SECURITY;
