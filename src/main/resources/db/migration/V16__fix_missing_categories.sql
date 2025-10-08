-- Fix missing categories for configurations
-- Update eu-north-1 to have storage category
UPDATE system_config 
SET category = 'storage' 
WHERE config_key = 'S3_REGION' AND config_value = 'eu-north-1' AND category IS NULL;

-- Update any other configurations that might be missing categories based on their config_key
UPDATE system_config 
SET category = 'storage' 
WHERE config_key IN ('STORAGE_TYPE', 'S3_BUCKET_NAME', 'S3_ACCESS_KEY', 'S3_SECRET_KEY', 'LOCAL_STORAGE_PATH') 
AND category IS NULL;

UPDATE system_config 
SET category = 'currency' 
WHERE config_key LIKE '%CURRENCY%' 
AND category IS NULL;

UPDATE system_config 
SET category = 'language' 
WHERE config_key LIKE '%LANGUAGE%' 
AND category IS NULL;

UPDATE system_config 
SET category = 'security' 
WHERE config_key LIKE '%SECURITY%' OR config_key LIKE '%AUTH%' 
AND category IS NULL;

-- Set default category for any remaining null categories
UPDATE system_config 
SET category = 'general' 
WHERE category IS NULL;
