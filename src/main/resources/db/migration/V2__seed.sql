-- V2__seed.sql
-- Seed con UUIDs fijos (para que cuadren las FKs). Fechas = TIMESTAMP (sin TZ).

SET search_path TO hort;

-- UUIDs fijos para el demo (puedes cambiarlos si quieres)
-- HORT (viene de Keycloak normalmente)
-- Hort Demo Leipzig
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM hort WHERE id = '11111111-1111-1111-1111-111111111111') THEN
    INSERT INTO hort (id, name)
    VALUES ('11111111-1111-1111-1111-111111111111', 'Hort Demo Leipzig');
  END IF;
END $$;

-- Fijamos el tenant para esta sesión (RLS)
SELECT set_config('app.hort_id', '11111111-1111-1111-1111-111111111111', true);

-- Personas
-- p1..p10
INSERT INTO person (id, hort_id, phone, first_name, last_name, address)
VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1','11111111-1111-1111-1111-111111111111','017455555','Anna','Muller','Hauptstrasse 5, Leipzig'),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2','11111111-1111-1111-1111-111111111111','017455555','Matilda','Muller','Hauptstrasse 5, Leipzig'),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3','11111111-1111-1111-1111-111111111111','017455555','Gina','Muller','Hauptstrasse 5, Leipzig'),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4','11111111-1111-1111-1111-111111111111','017455555','Rosa','Muller','Hauptstrasse 5, Leipzig'),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5','11111111-1111-1111-1111-111111111111','017455555','Elisabeth','Muller','Hauptstrasse 5, Leipzig'),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa6','11111111-1111-1111-1111-111111111111','017455555','Larifea','Muller','Hauptstrasse 5, Leipzig'),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa7','11111111-1111-1111-1111-111111111111','017455555','Wilhelm','Muller','Hauptstrasse 5, Leipzig'),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa8','11111111-1111-1111-1111-111111111111','017455555','Paul','Muller','Hauptstrasse 5, Leipzig'),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa9','11111111-1111-1111-1111-111111111111','017455555','Paul','Heinz','Hauptstrasse 5, Leipzig'),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa10','11111111-1111-1111-1111-111111111111','017455555','Paul','Heinzin','Hauptstrasse 5, Leipzig')
ON CONFLICT (id) DO NOTHING;

-- Grupos (g1..g12)
INSERT INTO hort_group (id, hort_id, name)
VALUES
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1','11111111-1111-1111-1111-111111111111','Gruppe 1A'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2','11111111-1111-1111-1111-111111111111','Gruppe 1B'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb3','11111111-1111-1111-1111-111111111111','Gruppe 1C'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb4','11111111-1111-1111-1111-111111111111','Gruppe 1D'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb5','11111111-1111-1111-1111-111111111111','Gruppe 2A'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb6','11111111-1111-1111-1111-111111111111','Gruppe 2B'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb7','11111111-1111-1111-1111-111111111111','Gruppe 2C'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb8','11111111-1111-1111-1111-111111111111','Gruppe 2D'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb9','11111111-1111-1111-1111-111111111111','Gruppe 3A'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbc10','11111111-1111-1111-1111-111111111111','Gruppe 3B'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbc11','11111111-1111-1111-1111-111111111111','Gruppe 3TM1'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbc12','11111111-1111-1111-1111-111111111111','Gruppe 3TM2')
ON CONFLICT (id) DO NOTHING;

-- Students (s1..s10)
INSERT INTO student (id, hort_id, group_id, person_id, can_leave_alone)
VALUES
  ('cccccccc-cccc-cccc-cccc-ccccccccccc1','11111111-1111-1111-1111-111111111111','bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2','aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1',false),
  ('cccccccc-cccc-cccc-cccc-ccccccccccc2','11111111-1111-1111-1111-111111111111','bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2','aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2',false),
  ('cccccccc-cccc-cccc-cccc-ccccccccccc3','11111111-1111-1111-1111-111111111111','bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2','aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3',false),
  ('cccccccc-cccc-cccc-cccc-ccccccccccc4','11111111-1111-1111-1111-111111111111','bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2','aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4',false),
  ('cccccccc-cccc-cccc-cccc-ccccccccccc5','11111111-1111-1111-1111-111111111111','bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2','aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5',false),
  ('cccccccc-cccc-cccc-cccc-ccccccccccc6','11111111-1111-1111-1111-111111111111','bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2','aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa6',false),
  ('cccccccc-cccc-cccc-cccc-ccccccccccc7','11111111-1111-1111-1111-111111111111','bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2','aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa7',false),
  ('cccccccc-cccc-cccc-cccc-ccccccccccc8','11111111-1111-1111-1111-111111111111','bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2','aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa8',false),
  ('cccccccc-cccc-cccc-cccc-ccccccccccc9','11111111-1111-1111-1111-111111111111','bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb3','aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa9',false)

ON CONFLICT (id) DO NOTHING;

-- Puedes añadir aquí seeds para collector/pickup_right/self_dismissal/check_out si lo necesitas.
