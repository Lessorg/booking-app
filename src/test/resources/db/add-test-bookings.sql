-- Insert roles
INSERT INTO roles (id, name) VALUES (1, 'ROLE_CUSTOMER');
INSERT INTO roles (id, name) VALUES (2, 'ROLE_ADMIN');

-- Insert users
INSERT INTO users (id, email, password, first_name, last_name)
VALUES
    (20, 'john.doe@example.com', '$2a$10$randomHash1', 'John', 'Doe'),
    (21, 'jane.smith@example.com', '$2a$10$randomHash2', 'Jane', 'Smith');

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

-- Assign roles to users
INSERT INTO users_roles (user_id, role_id) VALUES (20, 1);
INSERT INTO users_roles (user_id, role_id) VALUES (21, 1);

-- Insert accommodations
INSERT INTO accommodations (id, type, location, size, daily_rate, availability)
VALUES
    (10, 'HOTEL', 'New York', 'Medium', 120.00, 5),
    (11, 'APARTMENT', 'Los Angeles', 'Large', 150.00, 3);

SELECT setval('accommodations_id_seq', (SELECT MAX(id) FROM accommodations));

-- Insert accommodation amenities
INSERT INTO accommodation_amenities (accommodation_id, amenity)
VALUES
    (10, 'Free WiFi'),
    (10, 'Breakfast Included'),
    (10, 'Swimming Pool'),
    (11, 'Kitchen'),
    (11, 'Parking'),
    (11, 'Balcony');

-- Insert bookings
INSERT INTO bookings (id, check_in_date, check_out_date, accommodation_id, user_id, status)
VALUES
    (100, '2025-06-01', '2025-06-07', 10, 20, 'PENDING'),
    (101, '2025-07-10', '2025-07-15', 11, 21, 'CONFIRMED'),
    (102, '2025-02-20', '2025-02-25', 10, 20, 'CONFIRMED'),
    (103, '2025-02-18', '2025-02-22', 11, 21, 'CANCELED');

SELECT setval('bookings_id_seq', (SELECT MAX(id) FROM bookings));
