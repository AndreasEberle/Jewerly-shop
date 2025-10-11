-- Add jewelry-specific fields to products table
ALTER TABLE products 
ADD COLUMN ring_size VARCHAR(20),
ADD COLUMN chain_length VARCHAR(20),
ADD COLUMN color VARCHAR(50),
ADD COLUMN finish VARCHAR(50);

-- Add comments for documentation
COMMENT ON COLUMN products.ring_size IS 'Ring size for ring products (e.g., 7, 7.5, Adjustable, N/A)';
COMMENT ON COLUMN products.chain_length IS 'Chain length for necklace/bracelet products (e.g., 45cm, 18", Adjustable, N/A)';
COMMENT ON COLUMN products.color IS 'Primary color of the jewelry piece';
COMMENT ON COLUMN products.finish IS 'Surface finish of the jewelry (e.g., Polished, Matte, Brushed)';
