-- Add slug column to products table
ALTER TABLE products ADD COLUMN IF NOT EXISTS slug VARCHAR(255);

-- Create index on slug for faster lookups
CREATE INDEX IF NOT EXISTS idx_products_slug ON products(slug);

-- Generate slugs from existing product names (lowercase, replace spaces with hyphens, remove special chars)
UPDATE products 
SET slug = LOWER(REGEXP_REPLACE(REGEXP_REPLACE(name, '[^a-zA-Z0-9\s-]', '', 'g'), '\s+', '-', 'g'))
WHERE slug IS NULL OR slug = '';

-- Make slug unique and not null
ALTER TABLE products ALTER COLUMN slug SET NOT NULL;
ALTER TABLE products ADD CONSTRAINT unique_product_slug UNIQUE (slug);


