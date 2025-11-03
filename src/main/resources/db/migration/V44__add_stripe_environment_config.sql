-- Add Stripe environment configuration (test or prod)
-- Default value is 'test' for safety
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'stripe.environment', 'test', 'Stripe environment: "test" for sandbox/test mode, "prod" or "live" for production mode. Default: test', true, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM system_config 
    WHERE config_key = 'stripe.environment'
);



