-- Insert roles
INSERT INTO roles (id, name) VALUES (1, 'ROLE_CUSTOMER');

SELECT setval('roles_id_seq', (SELECT MAX(id) FROM roles));
