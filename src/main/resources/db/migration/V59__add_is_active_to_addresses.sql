-- Add is_active column to addresses table for soft delete functionality
ALTER TABLE addresses ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;

-- Update existing addresses to be active by default
UPDATE addresses SET is_active = TRUE WHERE is_active IS NULL;

