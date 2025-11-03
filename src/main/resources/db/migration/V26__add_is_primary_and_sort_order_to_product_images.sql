-- Add is_primary and sort_order columns to product_images table if they don't exist
DO $$ 
BEGIN
    -- Add is_primary column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'product_images' AND column_name = 'is_primary') THEN
        ALTER TABLE product_images ADD COLUMN is_primary BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
    
    -- Add sort_order column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'product_images' AND column_name = 'sort_order') THEN
        ALTER TABLE product_images ADD COLUMN sort_order INTEGER NOT NULL DEFAULT 0;
    END IF;
END $$;

-- Set the first image of each product as primary if no primary exists
UPDATE product_images 
SET is_primary = TRUE 
WHERE id IN (
    SELECT DISTINCT ON (product_id) id 
    FROM product_images 
    WHERE is_primary = FALSE
    ORDER BY product_id, created_at ASC
);

-- Set sort order based on creation time for images without sort order
UPDATE product_images 
SET sort_order = subquery.row_number - 1
FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY product_id ORDER BY created_at ASC) as row_number
    FROM product_images
) AS subquery
WHERE product_images.id = subquery.id;

-- Add comments
COMMENT ON COLUMN product_images.is_primary IS 'Indicates if this is the primary/featured image for the product';
COMMENT ON COLUMN product_images.sort_order IS 'Order of the image for display purposes';



