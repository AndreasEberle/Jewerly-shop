-- Test data for integration tests
-- This will be loaded before each test method

-- Create test categories
INSERT INTO categories (id, name, slug, description) VALUES 
(1, 'Rings', 'rings', 'Beautiful rings for every occasion'),
(2, 'Necklaces', 'necklaces', 'Elegant necklaces and pendants'),
(3, 'Earrings', 'earrings', 'Stunning earrings for all styles');

-- Create test tags
INSERT INTO tags (id, name, slug, description, created_at, updated_at) VALUES 
(1, 'Gold', 'gold', 'Gold jewelry pieces', NOW(), NOW()),
(2, 'Silver', 'silver', 'Silver jewelry pieces', NOW(), NOW()),
(3, 'Diamond', 'diamond', 'Diamond jewelry pieces', NOW(), NOW()),
(4, 'Vintage', 'vintage', 'Vintage style jewelry', NOW(), NOW());

-- Create test roles
INSERT INTO roles (id, name) VALUES 
(1, 'ADMIN'),
(2, 'USER'),
(3, 'CUSTOMER');

-- Create test users
INSERT INTO users (id, email, password_hash, first_name, last_name, phone, is_active, created_at, updated_at) VALUES 
('11111111-1111-1111-1111-111111111111', 'admin@jewelry.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVqgO1F1uQj7Oq7Oq7Oq7Oq7Oq', 'Admin', 'User', '+1234567890', true, NOW(), NOW()),
('22222222-2222-2222-2222-222222222222', 'customer@jewelry.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVqgO1F1uQj7Oq7Oq7Oq7Oq7Oq', 'John', 'Doe', '+1234567891', true, NOW(), NOW()),
('33333333-3333-3333-3333-333333333333', 'jane@jewelry.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVqgO1F1uQj7Oq7Oq7Oq7Oq7Oq', 'Jane', 'Smith', '+1234567892', true, NOW(), NOW());

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id) VALUES 
('11111111-1111-1111-1111-111111111111', 1), -- Admin user
('22222222-2222-2222-2222-222222222222', 2), -- Regular user
('33333333-3333-3333-3333-333333333333', 3); -- Customer

-- Create test products
INSERT INTO products (id, sku, name, description, price_cents, currency, material, gemstone, weight_grams, is_active, created_at, updated_at) VALUES 
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'RING001', 'Diamond Engagement Ring', 'Beautiful diamond engagement ring with 1 carat center stone', 500000, 'EUR', 'White Gold', 'Diamond', 3.5, true, NOW(), NOW()),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'NECK001', 'Pearl Necklace', 'Elegant pearl necklace with 18-inch chain', 250000, 'EUR', 'Silver', 'Pearl', 15.2, true, NOW(), NOW()),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'EARR001', 'Gold Hoop Earrings', 'Classic gold hoop earrings, 14k gold', 75000, 'EUR', 'Gold', 'None', 8.1, true, NOW(), NOW()),
('dddddddd-dddd-dddd-dddd-dddddddddddd', 'RING002', 'Vintage Ruby Ring', 'Antique-style ruby ring with intricate details', 180000, 'EUR', 'Gold', 'Ruby', 4.2, false, NOW(), NOW());

-- Link products to categories
INSERT INTO product_categories (product_id, category_id) VALUES 
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 1), -- Diamond ring -> Rings
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 2), -- Pearl necklace -> Necklaces
('cccccccc-cccc-cccc-cccc-cccccccccccc', 3), -- Gold earrings -> Earrings
('dddddddd-dddd-dddd-dddd-dddddddddddd', 1); -- Ruby ring -> Rings

-- Link products to tags
INSERT INTO product_tags (product_id, tag_id) VALUES 
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 1), -- Diamond ring -> Gold
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 3), -- Diamond ring -> Diamond
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 2), -- Pearl necklace -> Silver
('cccccccc-cccc-cccc-cccc-cccccccccccc', 1), -- Gold earrings -> Gold
('dddddddd-dddd-dddd-dddd-dddddddddddd', 1), -- Ruby ring -> Gold
('dddddddd-dddd-dddd-dddd-dddddddddddd', 4); -- Ruby ring -> Vintage

-- Create inventory
INSERT INTO inventory (product_id, quantity, reserved, updated_at) VALUES 
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 5, 0, NOW()),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 3, 1, NOW()),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 10, 0, NOW()),
('dddddddd-dddd-dddd-dddd-dddddddddddd', 0, 0, NOW());

-- Create test product images
INSERT INTO product_images (id, product_id, storage_key, url, is_primary, sort_order, alt_text, width, height, mime_type, created_at, updated_at) VALUES 
('img1111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'products/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa/ring1.webp', 'http://localhost:8080/files/products/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa/ring1.webp', true, 0, 'Diamond engagement ring front view', 800, 600, 'image/webp', NOW(), NOW()),
('img2222-2222-2222-2222-222222222222', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'products/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa/ring2.webp', 'http://localhost:8080/files/products/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa/ring2.webp', false, 1, 'Diamond engagement ring side view', 800, 600, 'image/webp', NOW(), NOW()),
('img3333-3333-3333-3333-333333333333', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'products/bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb/necklace1.webp', 'http://localhost:8080/files/products/bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb/necklace1.webp', true, 0, 'Pearl necklace on display', 800, 600, 'image/webp', NOW(), NOW()),
('img4444-4444-4444-4444-444444444444', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'products/cccccccc-cccc-cccc-cccc-cccccccccccc/earrings1.webp', 'http://localhost:8080/files/products/cccccccc-cccc-cccc-cccc-cccccccccccc/earrings1.webp', true, 0, 'Gold hoop earrings', 600, 600, 'image/webp', NOW(), NOW());
