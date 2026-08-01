SET search_path TO hort;

-- Composite candidate keys let foreign keys include the tenant boundary.
ALTER TABLE person ADD CONSTRAINT uk_person_id_hort UNIQUE (id, hort_id);
ALTER TABLE hort_group ADD CONSTRAINT uk_hort_group_id_hort UNIQUE (id, hort_id);
ALTER TABLE student ADD CONSTRAINT uk_student_id_hort UNIQUE (id, hort_id);
ALTER TABLE collector ADD CONSTRAINT uk_collector_id_hort UNIQUE (id, hort_id);
ALTER TABLE pickup_right ADD CONSTRAINT uk_pickup_right_id_hort UNIQUE (id, hort_id);
ALTER TABLE self_dismissal ADD CONSTRAINT uk_self_dismissal_id_hort UNIQUE (id, hort_id);

ALTER TABLE student
  ADD CONSTRAINT fk_student_person_hort
  FOREIGN KEY (person_id, hort_id) REFERENCES person(id, hort_id),
  ADD CONSTRAINT fk_student_group_hort
  FOREIGN KEY (group_id, hort_id) REFERENCES hort_group(id, hort_id);

ALTER TABLE collector
  ADD CONSTRAINT fk_collector_person_hort
  FOREIGN KEY (person_id, hort_id) REFERENCES person(id, hort_id);

ALTER TABLE pickup_right
  ADD CONSTRAINT fk_pickup_right_student_hort
  FOREIGN KEY (student_id, hort_id) REFERENCES student(id, hort_id),
  ADD CONSTRAINT fk_pickup_right_collector_hort
  FOREIGN KEY (collector_id, hort_id) REFERENCES collector(id, hort_id);

ALTER TABLE self_dismissal
  ADD CONSTRAINT fk_self_dismissal_student_hort
  FOREIGN KEY (student_id, hort_id) REFERENCES student(id, hort_id);

ALTER TABLE check_out
  ADD CONSTRAINT fk_check_out_student_hort
  FOREIGN KEY (student_id, hort_id) REFERENCES student(id, hort_id),
  ADD CONSTRAINT fk_check_out_collector_hort
  FOREIGN KEY (collector_id, hort_id) REFERENCES collector(id, hort_id),
  ADD CONSTRAINT fk_check_out_pickup_right_hort
  FOREIGN KEY (pickup_right_id, hort_id) REFERENCES pickup_right(id, hort_id),
  ADD CONSTRAINT fk_check_out_self_dismissal_hort
  FOREIGN KEY (self_dismissal_id, hort_id) REFERENCES self_dismissal(id, hort_id);

-- Runtime grants are conditional because infrastructure creates login roles.
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'hort_app') THEN
    GRANT USAGE ON SCHEMA hort TO hort_app;
    GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA hort TO hort_app;
    GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA hort TO hort_app;
    ALTER DEFAULT PRIVILEGES IN SCHEMA hort
      GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO hort_app;
    ALTER DEFAULT PRIVILEGES IN SCHEMA hort
      GRANT USAGE, SELECT ON SEQUENCES TO hort_app;
  END IF;
END $$;
