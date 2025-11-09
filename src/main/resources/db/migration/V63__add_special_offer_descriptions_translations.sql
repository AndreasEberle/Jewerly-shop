-- Add language-specific translations for special offer descriptions

-- English (en-US) - Limited Time Offer
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'special_offer.limited_time_offer.en-US', 'Limited Time Offer', 'Translation for "Limited Time Offer" in English', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'special_offer.limited_time_offer.en-US');

-- German (de-DE) - Limited Time Offer
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'special_offer.limited_time_offer.de-DE', 'Begrenztes Angebot', 'Translation for "Limited Time Offer" in German', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'special_offer.limited_time_offer.de-DE');

-- Japanese (ja-JP) - Limited Time Offer
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'special_offer.limited_time_offer.ja-JP', '期間限定オファー', 'Translation for "Limited Time Offer" in Japanese', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'special_offer.limited_time_offer.ja-JP');

-- English (en-US) - Free Shipping
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'special_offer.free_shipping.en-US', 'Free Shipping', 'Translation for "Free Shipping" in English', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'special_offer.free_shipping.en-US');

-- German (de-DE) - Free Shipping
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'special_offer.free_shipping.de-DE', 'Kostenloser Versand', 'Translation for "Free Shipping" in German', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'special_offer.free_shipping.de-DE');

-- Japanese (ja-JP) - Free Shipping
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'special_offer.free_shipping.ja-JP', '送料無料', 'Translation for "Free Shipping" in Japanese', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'special_offer.free_shipping.ja-JP');

-- Add more common special offer descriptions translations
-- Flash Sale
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'special_offer.flash_sale.en-US', 'Flash Sale', 'Translation for "Flash Sale" in English', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'special_offer.flash_sale.en-US');

INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'special_offer.flash_sale.de-DE', 'Blitzverkauf', 'Translation for "Flash Sale" in German', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'special_offer.flash_sale.de-DE');

INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'special_offer.flash_sale.ja-JP', 'フラッシュセール', 'Translation for "Flash Sale" in Japanese', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'special_offer.flash_sale.ja-JP');

-- Holiday Special
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'special_offer.holiday_special.en-US', 'Holiday Special', 'Translation for "Holiday Special" in English', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'special_offer.holiday_special.en-US');

INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'special_offer.holiday_special.de-DE', 'Feiertagsangebot', 'Translation for "Holiday Special" in German', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'special_offer.holiday_special.de-DE');

INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'special_offer.holiday_special.ja-JP', 'ホリデースペシャル', 'Translation for "Holiday Special" in Japanese', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'special_offer.holiday_special.ja-JP');

-- Clearance
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'special_offer.clearance.en-US', 'Clearance', 'Translation for "Clearance" in English', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'special_offer.clearance.en-US');

INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'special_offer.clearance.de-DE', 'Räumungsverkauf', 'Translation for "Clearance" in German', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'special_offer.clearance.de-DE');

INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'special_offer.clearance.ja-JP', 'クリアランス', 'Translation for "Clearance" in Japanese', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'special_offer.clearance.ja-JP');

-- New Arrival
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'special_offer.new_arrival.en-US', 'New Arrival', 'Translation for "New Arrival" in English', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'special_offer.new_arrival.en-US');

INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'special_offer.new_arrival.de-DE', 'Neu eingetroffen', 'Translation for "New Arrival" in German', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'special_offer.new_arrival.de-DE');

INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'special_offer.new_arrival.ja-JP', '新着', 'Translation for "New Arrival" in Japanese', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'special_offer.new_arrival.ja-JP');

-- Best Seller
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'special_offer.best_seller.en-US', 'Best Seller', 'Translation for "Best Seller" in English', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'special_offer.best_seller.en-US');

INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'special_offer.best_seller.de-DE', 'Bestseller', 'Translation for "Best Seller" in German', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'special_offer.best_seller.de-DE');

INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'special_offer.best_seller.ja-JP', 'ベストセラー', 'Translation for "Best Seller" in Japanese', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'special_offer.best_seller.ja-JP');

