-- Add tracking link and estimated delivery days to orders table
ALTER TABLE orders 
    ADD COLUMN IF NOT EXISTS tracking_link VARCHAR(500),
    ADD COLUMN IF NOT EXISTS estimated_delivery_days INTEGER DEFAULT 4;

-- Create index for tracking link lookups
CREATE INDEX IF NOT EXISTS idx_orders_tracking_link ON orders(tracking_link) WHERE tracking_link IS NOT NULL;

