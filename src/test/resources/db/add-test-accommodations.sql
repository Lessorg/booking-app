INSERT INTO accommodations (id, type, location, size, daily_rate, availability)
VALUES (1, 'HOUSE', '123 Main St', '3 Bedroom', 150.00, 5);
INSERT INTO accommodations (id, type, location, size, daily_rate, availability)
VALUES (2, 'APARTMENT', '456 Elm St', '2 Bedroom', 100.00, 3);

INSERT INTO accommodation_amenities (accommodation_id, amenity)
VALUES (1, 'WiFi'), (1, 'Pool'), (1, 'Air Conditioning');

INSERT INTO accommodation_amenities (accommodation_id, amenity)
VALUES (2, 'WiFi'), (2, 'Gym'), (2, 'Elevator');
