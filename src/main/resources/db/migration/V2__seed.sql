SET search_path TO hort;

-- Crea un hort demo id=1
INSERT INTO hort(id, name) VALUES (1, 'Hort Demo Leipzig')
ON CONFLICT (id) DO NOTHING;

-- Fija el tenant a 1 para esta sesión (RLS)
SELECT set_config('app.hort_id', '1', true);

-- PERSON
INSERT INTO person (id, hort_id, phone, first_name, last_name, address) VALUES
(1,1,'017455555','Anna','Muller','Hauptstrasse 5, Leipzig'),
(2,1,'017455555','Matilda','Muller','Hauptstrasse 5, Leipzig'),
(3,1,'017455555','Gina','Muller','Hauptstrasse 5, Leipzig'),
(4,1,'017455555','Rosa','Muller','Hauptstrasse 5, Leipzig'),
(5,1,'017455555','Elisabeth','Muller','Hauptstrasse 5, Leipzig'),
(6,1,'017455555','Larifea','Muller','Hauptstrasse 5, Leipzig'),
(7,1,'017455555','Wilhelm','Muller','Hauptstrasse 5, Leipzig'),
(8,1,'017455555','Paul','Muller','Hauptstrasse 5, Leipzig'),
(9,1,'017455555','Paul','Heinz','Hauptstrasse 5, Leipzig'),
(10,1,'017455555','Paul','Heinzin','Hauptstrasse 5, Leipzig')
ON CONFLICT (id) DO NOTHING;

-- GROUPS
INSERT INTO hort_group (id, hort_id, name) VALUES
(1,1,'Gruppe 1A'),(2,1,'Gruppe 1B'),(3,1,'Gruppe 1C'),(4,1,'Gruppe 1D'),
(5,1,'Gruppe 2A'),(6,1,'Gruppe 2B'),(7,1,'Gruppe 2C'),(8,1,'Gruppe 2D'),
(9,1,'Gruppe 3A'),(10,1,'Gruppe 3B'),(11,1,'Gruppe 3TM1'),(12,1,'Gruppe 3TM2')
ON CONFLICT (id) DO NOTHING;

-- STUDENTS
INSERT INTO student (id, hort_id, group_id, person_id, can_leave_alone) VALUES
(1,1,2,1,false),
(2,1,2,2,false),
(3,1,2,3,false),
(4,1,2,4,false),
(5,1,2,5,false),
(6,1,2,6,false),
(7,1,2,7,false),
(8,1,2,8,false),
(9,1,3,9,false),
(10,1,10,10,false)
ON CONFLICT (id) DO NOTHING;

-- (Opcional) más seeds (collector, pickup_right, etc.)
