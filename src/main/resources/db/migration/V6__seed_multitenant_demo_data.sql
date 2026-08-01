SET search_path TO hort;

-- Complete the HORT-1 demo with one collector, permission and checkout.
SELECT set_config('app.hort_id', '11111111-1111-1111-1111-111111111111', true);

INSERT INTO collector (id, hort_id, person_id, collector_type)
VALUES (
  'dddddddd-dddd-dddd-dddd-ddddddddddd1',
  '11111111-1111-1111-1111-111111111111',
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa10',
  'COLLECTOR'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO pickup_right (
  id, hort_id, student_id, collector_id, type, valid_from, status, main_collector
)
VALUES (
  'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1',
  '11111111-1111-1111-1111-111111111111',
  'cccccccc-cccc-cccc-cccc-ccccccccccc1',
  'dddddddd-dddd-dddd-dddd-ddddddddddd1',
  'PERMANENT', TIMESTAMP '2026-01-01 00:00:00', 'ACTIVE', true
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO check_out (
  id, hort_id, student_id, collector_type, collector_id, pickup_right_id,
  occurred_at, comment, recorded_by_user_id
)
VALUES (
  'ffffffff-ffff-ffff-ffff-fffffffffff1',
  '11111111-1111-1111-1111-111111111111',
  'cccccccc-cccc-cccc-cccc-ccccccccccc1',
  'COLLECTOR',
  'dddddddd-dddd-dddd-dddd-ddddddddddd1',
  'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1',
  TIMESTAMP '2026-01-02 15:00:00', 'HORT-1 demo checkout', 'seed-hort-1'
)
ON CONFLICT (id) DO NOTHING;

-- HORT-2 has intentionally overlapping group/person names to prove isolation.
SELECT set_config('app.hort_id', '22222222-2222-2222-2222-222222222222', true);

INSERT INTO hort_group (id, hort_id, name)
VALUES (
  '22222222-bbbb-bbbb-bbbb-bbbbbbbbbbb1',
  '22222222-2222-2222-2222-222222222222',
  'Gruppe 1B'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO person (id, hort_id, phone, first_name, last_name, address)
VALUES
  (
    '22222222-aaaa-aaaa-aaaa-aaaaaaaaaaa1',
    '22222222-2222-2222-2222-222222222222',
    '0202222001', 'Anna', 'Muller', 'HORT-2 Demo Address'
  ),
  (
    '22222222-aaaa-aaaa-aaaa-aaaaaaaaaaa2',
    '22222222-2222-2222-2222-222222222222',
    '0202222002', 'Paul', 'Heinzin', 'HORT-2 Collector Address'
  )
ON CONFLICT (id) DO NOTHING;

INSERT INTO student (id, hort_id, group_id, person_id, can_leave_alone)
VALUES (
  '22222222-cccc-cccc-cccc-ccccccccccc1',
  '22222222-2222-2222-2222-222222222222',
  '22222222-bbbb-bbbb-bbbb-bbbbbbbbbbb1',
  '22222222-aaaa-aaaa-aaaa-aaaaaaaaaaa1',
  false
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO collector (id, hort_id, person_id, collector_type)
VALUES (
  '22222222-dddd-dddd-dddd-ddddddddddd1',
  '22222222-2222-2222-2222-222222222222',
  '22222222-aaaa-aaaa-aaaa-aaaaaaaaaaa2',
  'COLLECTOR'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO pickup_right (
  id, hort_id, student_id, collector_id, type, valid_from, status, main_collector
)
VALUES (
  '22222222-eeee-eeee-eeee-eeeeeeeeeee1',
  '22222222-2222-2222-2222-222222222222',
  '22222222-cccc-cccc-cccc-ccccccccccc1',
  '22222222-dddd-dddd-dddd-ddddddddddd1',
  'PERMANENT', TIMESTAMP '2026-01-01 00:00:00', 'ACTIVE', true
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO check_out (
  id, hort_id, student_id, collector_type, collector_id, pickup_right_id,
  occurred_at, comment, recorded_by_user_id
)
VALUES (
  '22222222-ffff-ffff-ffff-fffffffffff1',
  '22222222-2222-2222-2222-222222222222',
  '22222222-cccc-cccc-cccc-ccccccccccc1',
  'COLLECTOR',
  '22222222-dddd-dddd-dddd-ddddddddddd1',
  '22222222-eeee-eeee-eeee-eeeeeeeeeee1',
  TIMESTAMP '2026-01-02 15:00:00', 'HORT-2 demo checkout', 'seed-hort-2'
)
ON CONFLICT (id) DO NOTHING;
