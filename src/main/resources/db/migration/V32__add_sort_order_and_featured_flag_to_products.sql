-- Add sort_order column to products table
ALTER TABLE products ADD COLUMN sort_order INTEGER DEFAULT 0;

-- Add show_in_featured flag to products table
ALTER TABLE products ADD COLUMN show_in_featured BOOLEAN DEFAULT false;

-- Update existing products with sequential sort_order based on created_at
UPDATE products 
SET sort_order = subquery.row_number
FROM (
    SELECT id, ROW_NUMBER() OVER (ORDER BY created_at ASC) as row_number
    FROM products
) AS subquery
WHERE products.id = subquery.id;

-- Set all existing products to show in featured by default
UPDATE products SET show_in_featured = true;

-- Create indexes for better performance
CREATE INDEX idx_products_sort_order ON products(sort_order);
CREATE INDEX idx_products_show_in_featured ON products(show_in_featured);

-- Add comments to the columns
COMMENT ON COLUMN products.sort_order IS 'Display order for products in carousel and listings';
COMMENT ON COLUMN products.show_in_featured IS 'Whether this product should be shown in the featured carousel';
