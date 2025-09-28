-- Simple test data for integration tests
-- This will be loaded before each test method

-- Create test categories
INSERT INTO categories (id, name, slug, description) VALUES 
('11111111-1111-1111-1111-111111111111', 'Rings', 'rings', 'Beautiful rings for every occasion'),
('22222222-2222-2222-2222-222222222222', 'Necklaces', 'necklaces', 'Elegant necklaces and pendants'),
('33333333-3333-3333-3333-333333333333', 'Earrings', 'earrings', 'Stunning earrings for all styles');

-- Create test tags
INSERT INTO tags (id, name, slug, description, created_at, updated_at) VALUES 
('44444444-4444-4444-4444-444444444444', 'Gold', 'gold', 'Gold jewelry pieces', NOW(), NOW()),
('55555555-5555-5555-5555-555555555555', 'Silver', 'silver', 'Silver jewelry pieces', NOW(), NOW()),
('66666666-6666-6666-6666-666666666666', 'Diamond', 'diamond', 'Diamond jewelry pieces', NOW(), NOW()),
('77777777-7777-7777-7777-777777777777', 'Vintage', 'vintage', 'Vintage style jewelry', NOW(), NOW());

-- Create test roles
INSERT INTO roles (id, name) VALUES 
('88888888-8888-8888-8888-888888888888', 'ADMIN'),
('99999999-9999-9999-9999-999999999999', 'USER'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'CUSTOMER');

-- Create test users
INSERT INTO users (id, email, password_hash, first_name, last_name, phone, is_active, totp_enabled, created_at, updated_at) VALUES 
('11111111-1111-1111-1111-111111111111', 'admin@jewelry.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVqgO1F1uQj7Oq7Oq7Oq7Oq7Oq', 'Admin', 'User', '+1234567890', true, false, NOW(), NOW()),
('22222222-2222-2222-2222-222222222222', 'customer@jewelry.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVqgO1F1uQj7Oq7Oq7Oq7Oq7Oq', 'John', 'Doe', '+1234567891', true, false, NOW(), NOW()),
('33333333-3333-3333-3333-333333333333', 'jane@jewelry.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVqgO1F1uQj7Oq7Oq7Oq7Oq7Oq', 'Jane', 'Smith', '+1234567892', true, false, NOW(), NOW());

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id) VALUES 
('11111111-1111-1111-1111-111111111111', '88888888-8888-8888-8888-888888888888'), -- Admin user
('22222222-2222-2222-2222-222222222222', '99999999-9999-9999-9999-999999999999'), -- Regular user
('33333333-3333-3333-3333-333333333333', 'cccccccc-cccc-cccc-cccc-cccccccccccc'); -- Customer

-- Create test products
INSERT INTO products (id, sku, name, description, price_cents, currency, material, gemstone, weight_grams, is_active, created_at, updated_at) VALUES 
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'RING001', 'Diamond Engagement Ring', 'Beautiful diamond engagement ring with 1 carat center stone', 500000, 'EUR', 'White Gold', 'Diamond', 3.5, true, NOW(), NOW()),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'NECK001', 'Pearl Necklace', 'Elegant pearl necklace with 18-inch chain', 250000, 'EUR', 'Silver', 'Pearl', 15.2, true, NOW(), NOW()),
('dddddddd-dddd-dddd-dddd-dddddddddddd', 'EARR001', 'Gold Hoop Earrings', 'Classic gold hoop earrings, 14k gold', 75000, 'EUR', 'Gold', 'None', 8.1, true, NOW(), NOW()),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'RING002', 'Vintage Ruby Ring', 'Antique-style ruby ring with intricate details', 180000, 'EUR', 'Gold', 'Ruby', 4.2, false, NOW(), NOW());

-- Link products to categories
INSERT INTO product_categories (product_id, category_id) VALUES 
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111'), -- Diamond ring -> Rings
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222'), -- Pearl necklace -> Necklaces
('dddddddd-dddd-dddd-dddd-dddddddddddd', '33333333-3333-3333-3333-333333333333'), -- Gold earrings -> Earrings
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '11111111-1111-1111-1111-111111111111'); -- Ruby ring -> Rings

-- Link products to tags
INSERT INTO product_tags (product_id, tag_id) VALUES 
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '44444444-4444-4444-4444-444444444444'), -- Diamond ring -> Gold
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '66666666-6666-6666-6666-666666666666'), -- Diamond ring -> Diamond
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '55555555-5555-5555-5555-555555555555'), -- Pearl necklace -> Silver
('dddddddd-dddd-dddd-dddd-dddddddddddd', '44444444-4444-4444-4444-444444444444'), -- Gold earrings -> Gold
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '44444444-4444-4444-4444-444444444444'), -- Ruby ring -> Gold
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '77777777-7777-7777-7777-777777777777'); -- Ruby ring -> Vintage

-- Create inventory
INSERT INTO inventory (product_id, quantity, reserved, updated_at) VALUES 
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 5, 0, NOW()),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 3, 1, NOW()),
('dddddddd-dddd-dddd-dddd-dddddddddddd', 10, 0, NOW()),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 0, 0, NOW());

-- Note: Product images are not included in this simple test data
-- Tests that require images will need to be updated or use a different test data file
