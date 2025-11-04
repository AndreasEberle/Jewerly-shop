-- Add auth modal marquee items configuration
-- Default value: comma-separated list of marquee items
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'auth.modal.marquee.items', 'Free Shipping Every Monday,Birthday Perks,Exclusive Product Access,Priority Sale Access', 'Comma-separated list of marquee items to display in the auth modal. Each item will scroll in the marquee carousel.', true, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM system_config 
    WHERE config_key = 'auth.modal.marquee.items'
);

