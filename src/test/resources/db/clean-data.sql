-- Delete all data
DELETE FROM users_roles;
DELETE FROM users;
DELETE FROM roles;

-- Reset sequences
SELECT setval('users_id_seq', 1, false);
SELECT setval('roles_id_seq', 1, false);