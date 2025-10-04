-- Update test users with clearer passwords and better data

-- Update existing test users
UPDATE users SET 
    first_name = 'Admin',
    last_name = 'User',
    phone_country_code = '+41',
    phone_number = '123456789',
    date_of_birth = '1990-01-01',
    gender = 'male'
WHERE email = 'admin@jewelryshop.com';

UPDATE users SET 
    first_name = 'John',
    last_name = 'Customer',
    phone_country_code = '+41',
    phone_number = '987654321',
    date_of_birth = '1985-05-15',
    gender = 'male'
WHERE email = 'customer@jewelryshop.com';

-- Add a third test user with different password
INSERT INTO users (email, password_hash, first_name, last_name, phone_country_code, phone_number, date_of_birth, gender, is_active)
VALUES 
    ('test@jewelryshop.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Test', 'User', '+41', '555666777', '1992-12-25', 'female', true)
ON CONFLICT (email) DO NOTHING;

-- Add user preferences for test users
INSERT INTO user_preferences (user_id, preferred_currency)
SELECT u.id, 'CHF' FROM users u WHERE u.email IN ('admin@jewelryshop.com', 'customer@jewelryshop.com', 'test@jewelryshop.com')
ON CONFLICT (user_id) DO NOTHING;
