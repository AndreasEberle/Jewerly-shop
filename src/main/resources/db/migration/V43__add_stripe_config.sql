-- Add default Stripe configuration entries (users should update with real keys via admin panel or env vars)
-- These are placeholder values that should be replaced with actual keys
-- Use WHERE NOT EXISTS to avoid duplicates since config_key is not unique (V7 removed unique constraint)
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'stripe.secret.key', '', 'Stripe Secret Key (sk_test_xxx or sk_live_xxx). Store in env var STRIPE_SECRET_KEY for security.', false, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM system_config 
    WHERE config_key = 'stripe.secret.key' 
    AND config_value = ''
);

INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'stripe.publishable.key', '', 'Stripe Publishable Key (pk_test_xxx or pk_live_xxx). Store in env var STRIPE_PUBLISHABLE_KEY for security.', false, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM system_config 
    WHERE config_key = 'stripe.publishable.key' 
    AND config_value = ''
);

