-- Add footer configuration to system_config
INSERT INTO system_config (config_key, config_value, description, is_active, created_at, updated_at) VALUES
-- Company Information
('footer.company_name', 'JewelryShop', 'Company name displayed in footer', true, NOW(), NOW()),
('footer.company_description', 'Discover our exquisite collection of premium jewelry. From elegant rings to stunning necklaces, we offer the finest pieces for every occasion.', 'Company description displayed in footer', true, NOW(), NOW()),

-- Quick Links (enable/disable)
('footer.quick_links.enabled', 'true', 'Enable or disable quick links section in footer', true, NOW(), NOW()),
('footer.quick_links.products.enabled', 'true', 'Enable Products link in quick links', true, NOW(), NOW()),
('footer.quick_links.products.label', 'All Products', 'Label for Products link', true, NOW(), NOW()),
('footer.quick_links.categories.enabled', 'true', 'Enable Categories link in quick links', true, NOW(), NOW()),
('footer.quick_links.categories.label', 'Categories', 'Label for Categories link', true, NOW(), NOW()),
('footer.quick_links.about.enabled', 'true', 'Enable About Us link in quick links', true, NOW(), NOW()),
('footer.quick_links.about.label', 'About Us', 'Label for About Us link', true, NOW(), NOW()),
('footer.quick_links.contact.enabled', 'true', 'Enable Contact link in quick links', true, NOW(), NOW()),
('footer.quick_links.contact.label', 'Contact', 'Label for Contact link', true, NOW(), NOW()),

-- Contact Information
('footer.contact.enabled', 'true', 'Enable or disable contact information section in footer', true, NOW(), NOW()),
('footer.contact.address.enabled', 'true', 'Enable address display in footer', true, NOW(), NOW()),
('footer.contact.address.value', '123 Jewelry Street, Luxury City', 'Physical address displayed in footer', true, NOW(), NOW()),
('footer.contact.phone.enabled', 'true', 'Enable phone number display in footer', true, NOW(), NOW()),
('footer.contact.phone.value', '+1 (555) 123-4567', 'Phone number displayed in footer', true, NOW(), NOW()),
('footer.contact.email.enabled', 'true', 'Enable email display in footer', true, NOW(), NOW()),
('footer.contact.email.value', 'info@jewelryshop.com', 'Email address displayed in footer', true, NOW(), NOW()),

-- Social Media Links
('footer.social.enabled', 'true', 'Enable or disable social media links section in footer', true, NOW(), NOW()),
('footer.social.facebook.enabled', 'true', 'Enable Facebook link in footer', true, NOW(), NOW()),
('footer.social.facebook.url', '#', 'Facebook profile URL', true, NOW(), NOW()),
('footer.social.instagram.enabled', 'true', 'Enable Instagram link in footer', true, NOW(), NOW()),
('footer.social.instagram.url', '#', 'Instagram profile URL', true, NOW(), NOW()),
('footer.social.twitter.enabled', 'true', 'Enable Twitter/X link in footer', true, NOW(), NOW()),
('footer.social.twitter.url', '#', 'Twitter/X profile URL', true, NOW(), NOW()),
('footer.social.linkedin.enabled', 'false', 'Enable LinkedIn link in footer', true, NOW(), NOW()),
('footer.social.linkedin.url', '#', 'LinkedIn profile URL', true, NOW(), NOW()),
('footer.social.youtube.enabled', 'false', 'Enable YouTube link in footer', true, NOW(), NOW()),
('footer.social.youtube.url', '#', 'YouTube channel URL', true, NOW(), NOW()),
('footer.social.pinterest.enabled', 'false', 'Enable Pinterest link in footer', true, NOW(), NOW()),
('footer.social.pinterest.url', '#', 'Pinterest profile URL', true, NOW(), NOW()),

-- Footer Bottom Text
('footer.copyright.enabled', 'true', 'Enable or disable copyright text in footer', true, NOW(), NOW()),
('footer.copyright.text', '© 2024 JewelryShop. All rights reserved.', 'Copyright text displayed in footer', true, NOW(), NOW()),
('footer.privacy_policy.enabled', 'false', 'Enable Privacy Policy link in footer', true, NOW(), NOW()),
('footer.privacy_policy.label', 'Privacy Policy', 'Label for Privacy Policy link', true, NOW(), NOW()),
('footer.privacy_policy.url', '/privacy-policy', 'Privacy Policy page URL', true, NOW(), NOW()),
('footer.terms_of_service.enabled', 'false', 'Enable Terms of Service link in footer', true, NOW(), NOW()),
('footer.terms_of_service.label', 'Terms of Service', 'Label for Terms of Service link', true, NOW(), NOW()),
('footer.terms_of_service.url', '/terms-of-service', 'Terms of Service page URL', true, NOW(), NOW())
ON CONFLICT (config_key) WHERE is_active = true DO NOTHING;

-- Add comments for documentation
COMMENT ON TABLE system_config IS 'System configuration settings including footer configuration';

