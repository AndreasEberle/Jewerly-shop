-- Add top banner carousel slides configuration
-- Slides are stored as JSON array: [{"text": "...", "link": "...", "linkText": "..."}, ...]
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'site.top_banner.slides', '[{"text": "Most Loved.", "link": "/products?featured=true", "linkText": "Shop Best Sellers"}, {"text": "", "link": "/faq/order-shipping", "linkText": "Free shipping on all intl. orders $150+"}]', 'JSON array of carousel slides for top banner', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'site.top_banner.slides');

