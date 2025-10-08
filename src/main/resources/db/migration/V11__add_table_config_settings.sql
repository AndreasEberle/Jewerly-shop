-- Add table configuration settings
INSERT INTO system_config (config_key, config_value, description, category) VALUES
('table.users.default_sort_column', 'createdAt', 'Default sort column for users table', 'table_config'),
('table.users.default_sort_direction', 'desc', 'Default sort direction for users table (asc/desc)', 'table_config'),
('table.users.default_visible_columns', 'user,contact,roles,status,authType,newsletter,emailVerified,profileCompleted,lastLogin,created,actions', 'Default visible columns for users table (comma-separated)', 'table_config'),

('table.orders.default_sort_column', 'createdAt', 'Default sort column for orders table', 'table_config'),
('table.orders.default_sort_direction', 'desc', 'Default sort direction for orders table (asc/desc)', 'table_config'),
('table.orders.default_visible_columns', 'orderNumber,customer,status,total,createdAt,actions', 'Default visible columns for orders table (comma-separated)', 'table_config'),

('table.products.default_sort_column', 'name', 'Default sort column for products table', 'table_config'),
('table.products.default_sort_direction', 'asc', 'Default sort direction for products table (asc/desc)', 'table_config'),
('table.products.default_visible_columns', 'name,price,category,status,createdAt,actions', 'Default visible columns for products table (comma-separated)', 'table_config'),

('table.payments.default_sort_column', 'createdAt', 'Default sort column for payments table', 'table_config'),
('table.payments.default_sort_direction', 'desc', 'Default sort direction for payments table (asc/desc)', 'table_config'),
('table.payments.default_visible_columns', 'paymentId,orderId,amount,status,method,createdAt,actions', 'Default visible columns for payments table (comma-separated)', 'table_config');
