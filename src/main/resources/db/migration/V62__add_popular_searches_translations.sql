-- Add language-specific popular searches configuration
-- These are the search terms shown in the search modal

-- English popular searches (default)
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'site.search.popular_searches.en-US', 'Necklace,Ring,Earrings,Bracelet,Gold,Silver', 'Comma-separated list of popular search terms for English (en-US)', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'site.search.popular_searches.en-US');

-- German popular searches
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'site.search.popular_searches.de-DE', 'Halskette,Ring,Ohrringe,Armband,Gold,Silber', 'Comma-separated list of popular search terms for German (de-DE)', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'site.search.popular_searches.de-DE');

-- Japanese popular searches
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at)
SELECT 'site.search.popular_searches.ja-JP', 'ネックレス,リング,イヤリング,ブレスレット,ゴールド,シルバー', 'Comma-separated list of popular search terms for Japanese (ja-JP)', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM system_config WHERE config_key = 'site.search.popular_searches.ja-JP');

