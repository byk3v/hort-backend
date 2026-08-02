SET search_path TO hort;

-- Existing non-production timestamps are interpreted as UTC.
ALTER TABLE pickup_right
  ALTER COLUMN valid_from TYPE TIMESTAMP WITH TIME ZONE USING valid_from AT TIME ZONE 'UTC',
  ALTER COLUMN valid_until TYPE TIMESTAMP WITH TIME ZONE USING valid_until AT TIME ZONE 'UTC';

ALTER TABLE self_dismissal
  ALTER COLUMN valid_from TYPE TIMESTAMP WITH TIME ZONE USING valid_from AT TIME ZONE 'UTC',
  ALTER COLUMN valid_until TYPE TIMESTAMP WITH TIME ZONE USING valid_until AT TIME ZONE 'UTC';

CREATE TABLE self_dismissal_weekly_rule (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  hort_id UUID NOT NULL REFERENCES hort(id) ON DELETE RESTRICT,
  self_dismissal_id UUID NOT NULL,
  day_of_week VARCHAR(9) NOT NULL,
  allowed_from_time TIME NOT NULL,
  created_by VARCHAR(64) NOT NULL DEFAULT 'system',
  created_date TIMESTAMP NOT NULL DEFAULT NOW(),
  last_modified_by VARCHAR(64),
  last_modified_date TIMESTAMP,
  CONSTRAINT ck_self_dismissal_weekday CHECK (
    day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY')
  ),
  CONSTRAINT uk_self_dismissal_weekday UNIQUE (self_dismissal_id, day_of_week),
  CONSTRAINT fk_self_dismissal_weekly_rule_tenant
    FOREIGN KEY (self_dismissal_id, hort_id) REFERENCES self_dismissal(id, hort_id)
);

CREATE INDEX idx_self_dismissal_weekly_rule_hort
  ON self_dismissal_weekly_rule(hort_id);

ALTER TABLE self_dismissal_weekly_rule ENABLE ROW LEVEL SECURITY;
ALTER TABLE self_dismissal_weekly_rule FORCE ROW LEVEL SECURITY;

CREATE POLICY p_self_dismissal_weekly_rule_by_hort ON self_dismissal_weekly_rule
  USING (hort_id = NULLIF(current_setting('app.hort_id', true), '')::uuid)
  WITH CHECK (hort_id = NULLIF(current_setting('app.hort_id', true), '')::uuid);

DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'hort_app') THEN
    GRANT SELECT, INSERT, UPDATE, DELETE ON self_dismissal_weekly_rule TO hort_app;
  END IF;
END $$;
