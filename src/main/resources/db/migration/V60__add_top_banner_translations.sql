-- Add language-specific top banner translations
-- Store translations for each language in system_config

-- English translations (default)
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'site.top_banner.slides.en-US', '[{"text": "Most Loved.", "link": "/products?featured=true", "linkText": "Shop Best Sellers"}, {"text": "", "link": "/faq/order-shipping", "linkText": "Free Shipping On All Intl. Orders $150+"}]', 'Top banner carousel slides for English (en-US)', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'site.top_banner.slides.en-US');

-- German translations
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'site.top_banner.slides.de-DE', '[{"text": "Am meisten geliebt.", "link": "/products?featured=true", "linkText": "Bestseller ansehen"}, {"text": "", "link": "/faq/order-shipping", "linkText": "Kostenloser Versand bei allen internationalen Bestellungen ab 150 $"}]', 'Top banner carousel slides for German (de-DE)', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'site.top_banner.slides.de-DE');

-- Japanese translations
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'site.top_banner.slides.ja-JP', '[{"text": "最も愛されています。", "link": "/products?featured=true", "linkText": "ベストセラーを見る"}, {"text": "", "link": "/faq/order-shipping", "linkText": "国際注文150ドル以上で無料配送"}]', 'Top banner carousel slides for Japanese (ja-JP)', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'site.top_banner.slides.ja-JP');

