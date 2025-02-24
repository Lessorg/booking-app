-- Insert roles
INSERT INTO roles (id, name) VALUES (1, 'ROLE_CUSTOMER');
INSERT INTO roles (id, name) VALUES (2, 'ROLE_ADMIN');

-- Insert users
INSERT INTO users (id, email, password, first_name, last_name)
VALUES (1, 'test@example.com', '$2a$10$CW/ZKykmWrReIYbtiFiblO6j1fxI0RyKx6ol9IOELX9I9utjRyfcu', 'John', 'Doe');

-- Assign roles to users
INSERT INTO users_roles (user_id, role_id) VALUES (1, 1);
