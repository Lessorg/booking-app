-- Insert roles
INSERT INTO roles (id, name) VALUES (1, 'ROLE_CUSTOMER');
INSERT INTO roles (id, name) VALUES (2, 'ROLE_ADMIN');

-- Insert users
INSERT INTO users (id, email, password, first_name, last_name)
VALUES
    (1, 'testuser1@example.com', '$2a$10$testHash', 'Test1', 'User1'),
    (2, 'testuser2@example.com', '$2a$10$testHash', 'Test2', 'User2'),
    (3, 'testuser3@example.com', '$2a$10$testHash', 'Test3', 'User3');

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

-- Assign roles to users
INSERT INTO users_roles (user_id, role_id) VALUES (1, 1);

-- Insert accommodations
INSERT INTO accommodations (id, type, location, size, daily_rate, availability)
VALUES
    (1, 'HOTEL', 'Test City', 'Small', 80.00, 10);

SELECT setval('accommodations_id_seq', (SELECT MAX(id) FROM accommodations));

-- Insert bookings
INSERT INTO bookings (id, user_id, check_in_date, check_out_date, accommodation_id, status)
VALUES
    (1, 1, '2025-03-01', '2025-03-05', 1, 'PENDING'),
    (2, 1, '2025-01-01', '2025-02-05', 1, 'EXPIRED'),
    (3, 2, '2025-07-01', '2025-08-05', 1, 'PENDING'),
    (4, 3, '2025-09-01', '2025-09-05', 1, 'PENDING'),
    (200, 3, '2026-07-01', '2026-08-05', 1, 'PENDING'),
    (201, 3, '2026-09-01', '2026-09-05', 1, 'PENDING');

SELECT setval('bookings_id_seq', (SELECT MAX(id) FROM bookings));

-- Insert payments
INSERT INTO payments (id, booking_id, status, session_url, session_id, amount)
VALUES
    (1, 1, 'PENDING', 'http://test-url.com/session1', 'test-session-1', 100.00),
    (2, 2, 'PAID', 'http://test-url.com/session2', 'test-session-2', 150.00),
    (3, 3, 'PENDING', 'http://test-url.com/session3', 'test-session-3', 150.00),
    (4, 4, 'PENDING', 'http://test-url.com/session4', 'test-session-4', 175.24),
    (5, 200, 'EXPIRED', 'http://test-url.com/session5', 'test-session-5', 175.24);

SELECT setval('payments_id_seq', (SELECT MAX(id) FROM payments));
