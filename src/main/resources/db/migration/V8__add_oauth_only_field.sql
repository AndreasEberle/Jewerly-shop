-- Add oauth_only field to users table

ALTER TABLE users ADD COLUMN IF NOT EXISTS oauth_only BOOLEAN NOT NULL DEFAULT FALSE;

-- Update existing OAuth users to have oauth_only = true
-- This is a simple approach - in production you'd want to identify OAuth users more precisely
UPDATE users SET oauth_only = true WHERE email LIKE '%@gmail.com' OR email LIKE '%@googlemail.com';
