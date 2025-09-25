-- Flyway V4: Insert test product for image testing

-- Insert a test product
INSERT INTO products (id, sku, name, description, price_cents, currency, material, gemstone, weight_grams, is_active, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'TEST-001',
    'Test Gold Ring',
    'A beautiful test gold ring for demonstration',
    50000, -- 500.00 EUR in cents
    'EUR',
    '18K Gold',
    'Diamond',
    5.2,
    true,
    NOW(),
    NOW()
);

-- Insert test image for the product
INSERT INTO product_images (id, product_id, storage_key, url, is_primary, sort_order, alt_text, width, height, mime_type, created_at)
VALUES (
    uuid_generate_v4(),
    '00000000-0000-0000-0000-000000000001',
    'products/00000000-0000-0000-0000-000000000001/test-image.webp',
    'http://localhost:8080/files/products/00000000-0000-0000-0000-000000000001/test-image.webp',
    true,
    0,
    'Test jewelry image',
    800,
    600,
    'image/webp',
    NOW()
);
