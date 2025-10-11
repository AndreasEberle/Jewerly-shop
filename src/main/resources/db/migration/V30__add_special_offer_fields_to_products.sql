-- Add special offer fields to products table
ALTER TABLE products
ADD COLUMN special_offer BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN special_offer_price_cents BIGINT,
ADD COLUMN special_offer_description TEXT;

-- Add comments for the new columns
COMMENT ON COLUMN products.special_offer IS 'Indicates if this product has a special offer';
COMMENT ON COLUMN products.special_offer_price_cents IS 'Special offer price in cents (e.g., 7999 for 79.99)';
COMMENT ON COLUMN products.special_offer_description IS 'Description of the special offer (comma-separated list of special offer description names)';

-- Add constraint to ensure special offer price is positive when special offer is true
ALTER TABLE products
ADD CONSTRAINT check_special_offer_price 
CHECK (
    (special_offer = FALSE) OR 
    (special_offer = TRUE AND special_offer_price_cents IS NOT NULL AND special_offer_price_cents > 0)
);
