-- ===========================================
-- UPDATE ADDRESS ENTITY
-- ===========================================
-- Add street and apartment fields to addresses table
-- ===========================================

-- Add new columns to addresses table
ALTER TABLE addresses 
ADD COLUMN street VARCHAR(255),
ADD COLUMN apartment VARCHAR(255);

-- Migrate existing data from line1 and line2 to street and apartment
UPDATE addresses 
SET street = line1,
    apartment = line2;

-- Make street column NOT NULL after data migration
ALTER TABLE addresses 
ALTER COLUMN street SET NOT NULL;

-- Remove old columns
ALTER TABLE addresses 
DROP COLUMN line1,
DROP COLUMN line2;

-- Add constraints
ALTER TABLE addresses 
ADD CONSTRAINT chk_address_street CHECK (LENGTH(street) > 0),
ADD CONSTRAINT chk_address_city CHECK (LENGTH(city) > 0),
ADD CONSTRAINT chk_address_postal_code CHECK (LENGTH(postal_code) > 0),
ADD CONSTRAINT chk_address_country CHECK (LENGTH(country) > 0);

-- Add indexes for better performance
CREATE INDEX idx_addresses_user_id ON addresses(user_id);
CREATE INDEX idx_addresses_city ON addresses(city);
CREATE INDEX idx_addresses_country ON addresses(country);
