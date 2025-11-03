-- Fix payment_status column to work with Hibernate @Enumerated(EnumType.STRING)
-- Change from PostgreSQL ENUM to VARCHAR to avoid casting issues (same approach as order_status)

-- Alter the column to VARCHAR (using USING to cast enum to text)
ALTER TABLE payments 
    ALTER COLUMN status TYPE VARCHAR(50) 
    USING status::text;

-- Update any NULL values to default
UPDATE payments SET status = 'PENDING' WHERE status IS NULL;

-- Ensure NOT NULL constraint (safe even if already NOT NULL)
ALTER TABLE payments 
    ALTER COLUMN status SET NOT NULL;

-- Drop existing check constraint if it exists (for idempotency)
ALTER TABLE payments DROP CONSTRAINT IF EXISTS payments_status_check;

-- Add check constraint to ensure valid values
ALTER TABLE payments 
    ADD CONSTRAINT payments_status_check 
    CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED'));

