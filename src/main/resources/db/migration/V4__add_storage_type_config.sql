-- Add storage type configuration
INSERT INTO system_config (config_key, config_value, description, created_at, updated_at)
VALUES (
    'STORAGE_TYPE', 
    's3', 
    'Storage type: local, s3, or hybrid (s3 primary with local fallback)', 
    NOW(), 
    NOW()
) ON CONFLICT (config_key) DO UPDATE SET 
    config_value = EXCLUDED.config_value,
    description = EXCLUDED.description,
    updated_at = NOW();
