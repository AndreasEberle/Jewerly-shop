-- Fix storage type description
UPDATE system_config 
SET description = 'Storage type: local, s3, or hybrid' 
WHERE config_key = 'STORAGE_TYPE' AND description LIKE '%Storage type: local, s3, or hybrid%';
