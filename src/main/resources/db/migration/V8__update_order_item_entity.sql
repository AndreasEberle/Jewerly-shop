-- ===========================================
-- UPDATE ORDER ITEM ENTITY
-- ===========================================
-- Add unit_price field to order_items table
-- ===========================================

-- Add unit_price column to order_items table
ALTER TABLE order_items 
ADD COLUMN unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00;

-- Update existing records to calculate unit_price from unit_price_cents
UPDATE order_items 
SET unit_price = unit_price_cents / 100.00;

-- Remove the old unit_price_cents column
ALTER TABLE order_items 
DROP COLUMN unit_price_cents;

-- Add constraints
ALTER TABLE order_items 
ADD CONSTRAINT chk_order_item_quantity CHECK (quantity > 0),
ADD CONSTRAINT chk_order_item_unit_price CHECK (unit_price >= 0);

-- Add indexes for better performance
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
