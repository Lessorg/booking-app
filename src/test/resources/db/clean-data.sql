-- clean-data.sql
DELETE FROM accommodation_amenities;
DELETE FROM accommodations;
DELETE FROM users_roles;
DELETE FROM users;
DELETE FROM roles;

-- Reset sequences
SELECT setval('users_id_seq', 1, false);
SELECT setval('roles_id_seq', 1, false);
SELECT setval('accommodations_id_seq', 1, false);
