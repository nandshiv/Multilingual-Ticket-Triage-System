-- Seed Customers (with fixed UUIDs for easy API testing)
INSERT INTO customers (id, name, tier, region, preferred_language) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Acme Corp', 'PLATINUM', 'North America', 'en'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Globex Corporation', 'GOLD', 'Europe', 'es'),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Initech', 'SILVER', 'Asia', 'ja'),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'Umbrella Corp', 'BRONZE', 'South America', 'pt');

-- Seed Teams with category mappings matching the classifier outputs
INSERT INTO teams (name, category_mapping) VALUES
('Billing Support', '{"categories": ["Billing"]}'),
('Technical Support', '{"categories": ["Technical"]}'),
('Account Security', '{"categories": ["Account"]}'),
('Customer Relations', '{"categories": ["Complaint"]}');

-- Seed Agents assigned to teams
INSERT INTO agents (name, team_id, current_load) VALUES
('Alice Smith', 1, 2),
('Bob Jones', 2, 5),
('Charlie Brown', 3, 1),
('Diana Prince', 4, 0);
