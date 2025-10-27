-- Nos aseguramos de estar en el schema correcto
SET search_path TO hort;

-- ===== Tipos enum =====
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'collector_type_enum') THEN
    CREATE TYPE collector_type_enum AS ENUM ('COLLECTOR','STUDENT'); -- ajustado a CollectorType.java
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'pickup_type_enum') THEN
    CREATE TYPE pickup_type_enum AS ENUM ('PERMANENT','DAILY'); -- orden según Java PermissionType
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'permission_status_enum') THEN
    CREATE TYPE permission_status_enum AS ENUM ('ACTIVE','REVOKED','EXPIRED');
  END IF;
END $$;

-- ===== Tenants (horts) =====
CREATE TABLE IF NOT EXISTS hort (
  id   BIGSERIAL PRIMARY KEY,
  name VARCHAR(160) NOT NULL UNIQUE
);

-- ===== Tablas de dominio (todas con hort_id) =====

CREATE TABLE IF NOT EXISTS person (
  id         BIGSERIAL PRIMARY KEY,
  hort_id    BIGINT NOT NULL REFERENCES hort(id) ON DELETE RESTRICT,
  phone      VARCHAR(40),
  first_name VARCHAR(120) NOT NULL,
  last_name  VARCHAR(120) NOT NULL,
  address    VARCHAR(250)
);

CREATE TABLE IF NOT EXISTS tutor (
  id        BIGSERIAL PRIMARY KEY,
  person_id BIGINT NOT NULL REFERENCES person(id),
  CONSTRAINT uk_tutor_person UNIQUE (person_id)
);

CREATE TABLE IF NOT EXISTS hort_group (
  id      BIGSERIAL PRIMARY KEY,
  hort_id BIGINT NOT NULL REFERENCES hort(id) ON DELETE RESTRICT,
  name    VARCHAR(160) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_hort_group_hort_name ON hort_group(hort_id, name);

CREATE TABLE IF NOT EXISTS student (
  id                    BIGSERIAL PRIMARY KEY,
  hort_id               BIGINT NOT NULL REFERENCES hort(id) ON DELETE RESTRICT,
  person_id             BIGINT NOT NULL REFERENCES person(id),
  group_id              BIGINT NOT NULL REFERENCES hort_group(id),
  allowed_time_to_leave DATE,
  can_leave_alone       BOOLEAN NOT NULL,
  CONSTRAINT uk_student_person UNIQUE (person_id)
);
CREATE INDEX IF NOT EXISTS idx_student_hort  ON student(hort_id);
CREATE INDEX IF NOT EXISTS idx_student_group ON student(group_id);

CREATE TABLE IF NOT EXISTS collector (
  id             BIGSERIAL PRIMARY KEY,
  hort_id        BIGINT NOT NULL REFERENCES hort(id) ON DELETE RESTRICT,
  person_id      BIGINT NOT NULL REFERENCES person(id),
  collector_type collector_type_enum NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_collector_hort ON collector(hort_id);

CREATE TABLE IF NOT EXISTS pickup_right (
  id                BIGSERIAL PRIMARY KEY,
  hort_id           BIGINT NOT NULL REFERENCES hort(id) ON DELETE RESTRICT,
  student_id        BIGINT NOT NULL REFERENCES student(id),
  collector_id      BIGINT NOT NULL REFERENCES collector(id),
  type              pickup_type_enum NOT NULL,
  valid_from        TIMESTAMP NOT NULL,
  valid_until       TIMESTAMP NULL,
  allowed_from_time TIME NULL,
  status            permission_status_enum NOT NULL,
  main_collector    BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_pickup_right_hort    ON pickup_right(hort_id);
CREATE INDEX IF NOT EXISTS idx_pickup_right_student ON pickup_right(student_id);

CREATE TABLE IF NOT EXISTS self_dismissal (
  id                BIGSERIAL PRIMARY KEY,
  student_id        BIGINT NOT NULL REFERENCES student(id),
  valid_from        TIMESTAMP NOT NULL,
  valid_until       TIMESTAMP NULL,
  allowed_from_time TIME NULL,
  status            permission_status_enum NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_self_dismissal_student ON self_dismissal(student_id);

CREATE TABLE IF NOT EXISTS check_out (
  id                   BIGSERIAL PRIMARY KEY,
  hort_id              BIGINT NOT NULL REFERENCES hort(id) ON DELETE RESTRICT,
  student_id           BIGINT NOT NULL REFERENCES student(id),
  collector_type       collector_type_enum NOT NULL,
  collector_id         BIGINT NULL REFERENCES collector(id),
  pickup_right_id      BIGINT NULL REFERENCES pickup_right(id),
  self_dismissal_id    BIGINT NULL REFERENCES self_dismissal(id),
  occurred_at          TIMESTAMP NOT NULL DEFAULT NOW(),
  comment              VARCHAR(500),
  recorded_by_user_id  VARCHAR(255) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_checkout_student_date ON check_out (student_id, occurred_at);
CREATE INDEX IF NOT EXISTS idx_checkout_hort         ON check_out (hort_id);

-- ===== RLS por hort_id (políticas explícitas, sin nombres reservados) =====

-- person
ALTER TABLE person ENABLE ROW LEVEL SECURITY;
DO $$BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname = 'hort' AND tablename='person' AND policyname='p_person_by_hort') THEN
    CREATE POLICY p_person_by_hort ON person
      USING (hort_id = NULLIF(current_setting('app.hort_id', true), '')::bigint);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname = 'hort' AND tablename='person' AND policyname='p_person_ins') THEN
    CREATE POLICY p_person_ins ON person
      FOR INSERT WITH CHECK (hort_id = NULLIF(current_setting('app.hort_id', true), '')::bigint);
  END IF;
END$$;

-- hort_group
ALTER TABLE hort_group ENABLE ROW LEVEL SECURITY;
DO $$BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname = 'hort' AND tablename='hort_group' AND policyname='p_hort_group_by_hort') THEN
    CREATE POLICY p_hort_group_by_hort ON hort_group
      USING (hort_id = NULLIF(current_setting('app.hort_id', true), '')::bigint);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname = 'hort' AND tablename='hort_group' AND policyname='p_hort_group_ins') THEN
    CREATE POLICY p_hort_group_ins ON hort_group
      FOR INSERT WITH CHECK (hort_id = NULLIF(current_setting('app.hort_id', true), '')::bigint);
  END IF;
END$$;

-- student
ALTER TABLE student ENABLE ROW LEVEL SECURITY;
DO $$BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname = 'hort' AND tablename='student' AND policyname='p_student_by_hort') THEN
    CREATE POLICY p_student_by_hort ON student
      USING (hort_id = NULLIF(current_setting('app.hort_id', true), '')::bigint);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname = 'hort' AND tablename='student' AND policyname='p_student_ins') THEN
    CREATE POLICY p_student_ins ON student
      FOR INSERT WITH CHECK (hort_id = NULLIF(current_setting('app.hort_id', true), '')::bigint);
  END IF;
END$$;

-- collector
ALTER TABLE collector ENABLE ROW LEVEL SECURITY;
DO $$BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname = 'hort' AND tablename='collector' AND policyname='p_collector_by_hort') THEN
    CREATE POLICY p_collector_by_hort ON collector
      USING (hort_id = NULLIF(current_setting('app.hort_id', true), '')::bigint);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname = 'hort' AND tablename='collector' AND policyname='p_collector_ins') THEN
    CREATE POLICY p_collector_ins ON collector
      FOR INSERT WITH CHECK (hort_id = NULLIF(current_setting('app.hort_id', true), '')::bigint);
  END IF;
END$$;

-- pickup_right
ALTER TABLE pickup_right ENABLE ROW LEVEL SECURITY;
DO $$BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname = 'hort' AND tablename='pickup_right' AND policyname='p_pickup_right_by_hort') THEN
    CREATE POLICY p_pickup_right_by_hort ON pickup_right
      USING (hort_id = NULLIF(current_setting('app.hort_id', true), '')::bigint);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname = 'hort' AND tablename='pickup_right' AND policyname='p_pickup_right_ins') THEN
    CREATE POLICY p_pickup_right_ins ON pickup_right
      FOR INSERT WITH CHECK (hort_id = NULLIF(current_setting('app.hort_id', true), '')::bigint);
  END IF;
END$$;

-- check_out
ALTER TABLE check_out ENABLE ROW LEVEL SECURITY;
DO $$BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname = 'hort' AND tablename='check_out' AND policyname='p_check_out_by_hort') THEN
    CREATE POLICY p_check_out_by_hort ON check_out
      USING (hort_id = NULLIF(current_setting('app.hort_id', true), '')::bigint);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname = 'hort' AND tablename='check_out' AND policyname='p_check_out_ins') THEN
    CREATE POLICY p_check_out_ins ON check_out
      FOR INSERT WITH CHECK (hort_id = NULLIF(current_setting('app.hort_id', true), '')::bigint);
  END IF;
END$$;
