-- V6: Add default roles for the application

-- Insert default roles
INSERT INTO roles (name) VALUES ('CUSTOMER') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ADMIN') ON CONFLICT (name) DO NOTHING;

-- Create default admin user (password: Admin123!)
-- Note: This password should be changed immediately in production
INSERT INTO users (id, email, password_hash, first_name, last_name, is_active) 
VALUES (
    uuid_generate_v4(),
    'admin@jewelryshop.com',
    '$2a$10$rT8L8qvkUOKkYjy8yxqLx.8K6E4jF5oJ9mUzVx7zJFzN3qYr8fKLy', -- BCrypt hash for "Admin123!"
    'System',
    'Administrator',
    true
) ON CONFLICT (email) DO NOTHING;

-- Assign admin role to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.email = 'admin@jewelryshop.com' 
AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;
