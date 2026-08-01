SET search_path TO hort;

-- This UUID is also the hort_id claim assigned to /HORT-2 users in Keycloak.
INSERT INTO hort (id, name)
VALUES ('22222222-2222-2222-2222-222222222222', 'Hort Demo 2')
ON CONFLICT (id) DO NOTHING;
