-- Update users table to replace phone column with separate phone fields
-- This migration handles the transition from single phone field to separate country code and number fields

-- Drop the old phone column only if it exists
DO $$ 
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'phone') THEN
        ALTER TABLE users DROP COLUMN phone;
    END IF;
END $$;
