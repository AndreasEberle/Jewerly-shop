-- Fix the unique constraint for background_images table
-- The previous constraint was causing issues when setting is_active=false for multiple records

-- Drop the problematic constraint
ALTER TABLE background_images DROP CONSTRAINT IF EXISTS unique_active_per_section;

-- Add a partial unique index that only applies to active records (is_active = true)
-- This ensures only one active image per section while allowing multiple inactive ones
CREATE UNIQUE INDEX unique_active_per_section_idx 
ON background_images (section_name) 
WHERE is_active = true;
