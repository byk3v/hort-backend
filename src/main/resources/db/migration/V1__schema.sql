-- V1__schema.sql
-- Esquema base con UUIDs, auditoría BaseEntity (excepto en 'hort'),
-- RLS por hort_id (UUID) y fechas como TIMESTAMP (sin TZ)

CREATE SCHEMA IF NOT EXISTS hort;
SET search_path TO hort;

-- Extensión para UUID aleatorios (por si quieres usarla más tarde)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ===== Tipos enum =====
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'collector_type_enum') THEN
    CREATE TYPE collector_type_enum AS ENUM ('COLLECTOR','STUDENT');
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'permission_type_enum') THEN
    CREATE TYPE permission_type_enum AS ENUM ('PERMANENT','DAILY');
  END IF;

  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'permission_status_enum') THEN
    CREATE TYPE permission_status_enum AS ENUM ('ACTIVE','REVOKED','EXPIRED');
  END IF;
END $$;

-- ===== Tenants (horts) =====
-- IMPORTANTE: 'hort' NO extiende BaseEntity; el ID lo abastece tu app (Keycloak)
CREATE TABLE IF NOT EXISTS hort (
  id   UUID PRIMARY KEY,
  name VARCHAR(160) NOT NULL UNIQUE
);

-- ===== Tablas de dominio (todas con hort_id) =====
-- Incluyen los 4 campos de auditoría de tu BaseEntity:
--   created_by VARCHAR(64) NOT NULL
--   created_date TIMESTAMP NOT NULL
--   last_modified_by VARCHAR(64) NULL
--   last_modified_date TIMESTAMP NULL
-- Para facilitar el seed, ponemos DEFAULTs en created_by/created_date; tu app los sobreescribirá.

CREATE TABLE IF NOT EXISTS person (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  hort_id      UUID NOT NULL REFERENCES hort(id) ON DELETE RESTRICT,
  phone        VARCHAR(40),
  first_name   VARCHAR(120) NOT NULL,
  last_name    VARCHAR(120) NOT NULL,
  address      VARCHAR(250),
  -- Auditoría (BaseEntity)
  created_by         VARCHAR(64)  NOT NULL DEFAULT 'system',
  created_date       TIMESTAMP    NOT NULL DEFAULT NOW(),
  last_modified_by   VARCHAR(64),
  last_modified_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tutor (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  person_id    UUID NOT NULL REFERENCES person(id),
  -- Auditoría
  created_by         VARCHAR(64)  NOT NULL DEFAULT 'system',
  created_date       TIMESTAMP    NOT NULL DEFAULT NOW(),
  last_modified_by   VARCHAR(64),
  last_modified_date TIMESTAMP,
  CONSTRAINT uk_tutor_person UNIQUE (person_id)
);

CREATE TABLE IF NOT EXISTS hort_group (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  hort_id      UUID NOT NULL REFERENCES hort(id) ON DELETE RESTRICT,
  name         VARCHAR(160) NOT NULL,
  -- Auditoría
  created_by         VARCHAR(64)  NOT NULL DEFAULT 'system',
  created_date       TIMESTAMP    NOT NULL DEFAULT NOW(),
  last_modified_by   VARCHAR(64),
  last_modified_date TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_hort_group_hort_name ON hort_group(hort_id, name);

CREATE TABLE IF NOT EXISTS student (
  id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  hort_id               UUID NOT NULL REFERENCES hort(id) ON DELETE RESTRICT,
  person_id             UUID NOT NULL REFERENCES person(id),
  group_id              UUID NOT NULL REFERENCES hort_group(id),
  allowed_time_to_leave TIMESTAMP,
  can_leave_alone       BOOLEAN NOT NULL,
  -- Auditoría
  created_by         VARCHAR(64)  NOT NULL DEFAULT 'system',
  created_date       TIMESTAMP    NOT NULL DEFAULT NOW(),
  last_modified_by   VARCHAR(64),
  last_modified_date TIMESTAMP,
  CONSTRAINT uk_student_person UNIQUE (person_id)
);
CREATE INDEX IF NOT EXISTS idx_student_hort  ON student(hort_id);
CREATE INDEX IF NOT EXISTS idx_student_group ON student(group_id);

CREATE TABLE IF NOT EXISTS collector (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  hort_id        UUID NOT NULL REFERENCES hort(id) ON DELETE RESTRICT,
  person_id      UUID NOT NULL REFERENCES person(id),
  collector_type collector_type_enum NOT NULL,
  -- Auditoría
  created_by         VARCHAR(64)  NOT NULL DEFAULT 'system',
  created_date       TIMESTAMP    NOT NULL DEFAULT NOW(),
  last_modified_by   VARCHAR(64),
  last_modified_date TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_collector_hort ON collector(hort_id);

CREATE TABLE IF NOT EXISTS pickup_right (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  hort_id           UUID NOT NULL REFERENCES hort(id) ON DELETE RESTRICT,
  student_id        UUID NOT NULL REFERENCES student(id),
  collector_id      UUID NOT NULL REFERENCES collector(id),
  type              permission_type_enum NOT NULL,
  valid_from        TIMESTAMP NOT NULL,
  valid_until       TIMESTAMP NULL,
  allowed_from_time TIME NULL,
  status            permission_status_enum NOT NULL,
  main_collector    BOOLEAN NOT NULL DEFAULT FALSE,
  -- Auditoría
  created_by         VARCHAR(64)  NOT NULL DEFAULT 'system',
  created_date       TIMESTAMP    NOT NULL DEFAULT NOW(),
  last_modified_by   VARCHAR(64),
  last_modified_date TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_pickup_right_hort    ON pickup_right(hort_id);
CREATE INDEX IF NOT EXISTS idx_pickup_right_student ON pickup_right(student_id);

CREATE TABLE IF NOT EXISTS self_dismissal (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id        UUID NOT NULL REFERENCES student(id),
  valid_from        TIMESTAMP NOT NULL,
  valid_until       TIMESTAMP NULL,
  allowed_from_time TIME NULL,
  status            permission_status_enum NOT NULL,
  -- Auditoría
  created_by         VARCHAR(64)  NOT NULL DEFAULT 'system',
  created_date       TIMESTAMP    NOT NULL DEFAULT NOW(),
  last_modified_by   VARCHAR(64),
  last_modified_date TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_self_dismissal_student ON self_dismissal(student_id);

CREATE TABLE IF NOT EXISTS check_out (
  id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  hort_id              UUID NOT NULL REFERENCES hort(id) ON DELETE RESTRICT,
  student_id           UUID NOT NULL REFERENCES student(id),
  collector_type       collector_type_enum NOT NULL,
  collector_id         UUID NULL REFERENCES collector(id),
  pickup_right_id      UUID NULL REFERENCES pickup_right(id),
  self_dismissal_id    UUID NULL REFERENCES self_dismissal(id),
  occurred_at          TIMESTAMP NOT NULL DEFAULT NOW(),
  comment              VARCHAR(500),
  recorded_by_user_id  VARCHAR(255) NOT NULL,
  -- Auditoría
  created_by         VARCHAR(64)  NOT NULL DEFAULT 'system',
  created_date       TIMESTAMP    NOT NULL DEFAULT NOW(),
  last_modified_by   VARCHAR(64),
  last_modified_date TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_checkout_student_date ON check_out (student_id, occurred_at);
CREATE INDEX IF NOT EXISTS idx_checkout_hort         ON check_out (hort_id);

-- ===== RLS por hort_id (UUID) =====
-- Usamos current_setting('app.hort_id')::uuid

-- person
ALTER TABLE person ENABLE ROW LEVEL SECURITY;
DO $$BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname='hort' AND tablename='person' AND policyname='p_person_by_hort') THEN
    CREATE POLICY p_person_by_hort ON person
      USING (hort_id = NULLIF(current_setting('app.hort_id', true), '')::uuid);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname='hort' AND tablename='person' AND policyname='p_person_ins') THEN
    CREATE POLICY p_person_ins ON person
      FOR INSERT WITH CHECK (hort_id = NULLIF(current_setting('app.hort_id', true), '')::uuid);
  END IF;
END$$;

-- hort_group
ALTER TABLE hort_group ENABLE ROW LEVEL SECURITY;
DO $$BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname='hort' AND tablename='hort_group' AND policyname='p_hort_group_by_hort') THEN
    CREATE POLICY p_hort_group_by_hort ON hort_group
      USING (hort_id = NULLIF(current_setting('app.hort_id', true), '')::uuid);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname='hort' AND tablename='hort_group' AND policyname='p_hort_group_ins') THEN
    CREATE POLICY p_hort_group_ins ON hort_group
      FOR INSERT WITH CHECK (hort_id = NULLIF(current_setting('app.hort_id', true), '')::uuid);
  END IF;
END$$;

-- student
ALTER TABLE student ENABLE ROW LEVEL SECURITY;
DO $$BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname='hort' AND tablename='student' AND policyname='p_student_by_hort') THEN
    CREATE POLICY p_student_by_hort ON student
      USING (hort_id = NULLIF(current_setting('app.hort_id', true), '')::uuid);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname='hort' AND tablename='student' AND policyname='p_student_ins') THEN
    CREATE POLICY p_student_ins ON student
      FOR INSERT WITH CHECK (hort_id = NULLIF(current_setting('app.hort_id', true), '')::uuid);
  END IF;
END$$;

-- collector
ALTER TABLE collector ENABLE ROW LEVEL SECURITY;
DO $$BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname='hort' AND tablename='collector' AND policyname='p_collector_by_hort') THEN
    CREATE POLICY p_collector_by_hort ON collector
      USING (hort_id = NULLIF(current_setting('app.hort_id', true), '')::uuid);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname='hort' AND tablename='collector' AND policyname='p_collector_ins') THEN
    CREATE POLICY p_collector_ins ON collector
      FOR INSERT WITH CHECK (hort_id = NULLIF(current_setting('app.hort_id', true), '')::uuid);
  END IF;
END$$;

-- pickup_right
ALTER TABLE pickup_right ENABLE ROW LEVEL SECURITY;
DO $$BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname='hort' AND tablename='pickup_right' AND policyname='p_pickup_right_by_hort') THEN
    CREATE POLICY p_pickup_right_by_hort ON pickup_right
      USING (hort_id = NULLIF(current_setting('app.hort_id', true), '')::uuid);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname='hort' AND tablename='pickup_right' AND policyname='p_pickup_right_ins') THEN
    CREATE POLICY p_pickup_right_ins ON pickup_right
      FOR INSERT WITH CHECK (hort_id = NULLIF(current_setting('app.hort_id', true), '')::uuid);
  END IF;
END$$;

-- check_out
ALTER TABLE check_out ENABLE ROW LEVEL SECURITY;
DO $$BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname='hort' AND tablename='check_out' AND policyname='p_check_out_by_hort') THEN
    CREATE POLICY p_check_out_by_hort ON check_out
      USING (hort_id = NULLIF(current_setting('app.hort_id', true), '')::uuid);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE schemaname='hort' AND tablename='check_out' AND policyname='p_check_out_ins') THEN
    CREATE POLICY p_check_out_ins ON check_out
      FOR INSERT WITH CHECK (hort_id = NULLIF(current_setting('app.hort_id', true), '')::uuid);
  END IF;
END$$;
