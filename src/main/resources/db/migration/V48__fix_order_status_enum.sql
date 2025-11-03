-- Fix order_status column to work with Hibernate @Enumerated(EnumType.STRING)
-- Change from PostgreSQL ENUM to VARCHAR to avoid casting issues

-- Alter the column to VARCHAR (using USING to cast enum to text)
ALTER TABLE orders 
    ALTER COLUMN status TYPE VARCHAR(50) 
    USING status::text;

-- Update any NULL values to default
UPDATE orders SET status = 'PENDING' WHERE status IS NULL;

-- Ensure NOT NULL constraint (safe even if already NOT NULL)
ALTER TABLE orders 
    ALTER COLUMN status SET NOT NULL;

-- Drop existing check constraint if it exists (for idempotency)
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check;

-- Add check constraint to ensure valid values
ALTER TABLE orders 
    ADD CONSTRAINT orders_status_check 
    CHECK (status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED'));
