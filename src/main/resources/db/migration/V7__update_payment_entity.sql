-- ===========================================
-- UPDATE PAYMENT ENTITY
-- ===========================================
-- Add missing fields to payments table
-- ===========================================

-- Add new columns to payments table
ALTER TABLE payments 
ADD COLUMN payment_method VARCHAR(50) NOT NULL DEFAULT 'demo',
ADD COLUMN amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
ADD COLUMN transaction_id VARCHAR(255),
ADD COLUMN processed_at TIMESTAMP,
ADD COLUMN failure_reason VARCHAR(500),
ADD COLUMN notes VARCHAR(1000);

-- Update status column to use new enum values
ALTER TABLE payments 
ALTER COLUMN status TYPE VARCHAR(20);

-- Update existing records to use new status values
UPDATE payments 
SET status = CASE 
    WHEN status = 'PENDING' THEN 'PENDING'
    WHEN status = 'COMPLETED' THEN 'SUCCESS'
    WHEN status = 'FAILED' THEN 'FAILED'
    WHEN status = 'REFUNDED' THEN 'REFUNDED'
    ELSE 'PENDING'
END;

-- Remove old columns that are no longer needed
ALTER TABLE payments 
DROP COLUMN IF EXISTS amount_cents,
DROP COLUMN IF EXISTS currency,
DROP COLUMN IF EXISTS provider,
DROP COLUMN IF EXISTS provider_ref;

-- Add constraints
ALTER TABLE payments 
ADD CONSTRAINT chk_payment_amount CHECK (amount >= 0),
ADD CONSTRAINT chk_payment_method CHECK (payment_method IN ('stripe', 'paypal', 'demo'));

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments(order_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_processed_at ON payments(processed_at);
