package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import andreas.kafkis.eberle.jewelry.shop.backend.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class PublicController {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    /**
     * Get website status and related configuration
     */
    @GetMapping("/website-status")
    public ResponseEntity<Map<String, Object>> getWebsiteStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            
            // Get website status
            String websiteStatus = systemConfigService.getConfigValue("website_status");
            status.put("status", websiteStatus != null ? websiteStatus : "normal");
            
            // Get status-specific messages and images
            if ("construction".equals(websiteStatus)) {
                String message = systemConfigService.getConfigValue("construction_message");
                String imageUrl = systemConfigService.getConfigValue("construction_image_url");
                status.put("message", message != null ? message : "We are currently working on improving our website. Please check back soon!");
                status.put("imageUrl", imageUrl);
            } else if ("vacation".equals(websiteStatus)) {
                String message = systemConfigService.getConfigValue("vacation_message");
                String imageUrl = systemConfigService.getConfigValue("vacation_image_url");
                String startDate = systemConfigService.getConfigValue("vacation_start_date");
                String endDate = systemConfigService.getConfigValue("vacation_end_date");
                
                status.put("message", message != null ? message : "We are currently on vacation and will be back soon!");
                status.put("imageUrl", imageUrl);
                status.put("startDate", startDate);
                status.put("endDate", endDate);
            }
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            // Return normal status if there's an error
            Map<String, Object> fallbackStatus = new HashMap<>();
            fallbackStatus.put("status", "normal");
            return ResponseEntity.ok(fallbackStatus);
        }
    }
    
    /**
     * Get footer configuration
     */
    @GetMapping("/footer-config")
    @Operation(summary = "Get footer configuration")
    public ResponseEntity<Map<String, Object>> getFooterConfig() {
        try {
            Map<String, Object> footerConfig = new HashMap<>();
            
            // Company Information
            footerConfig.put("companyName", systemConfigService.getConfigValue("footer.company_name"));
            footerConfig.put("companyDescription", systemConfigService.getConfigValue("footer.company_description"));
            
            // Quick Links
            Map<String, Object> quickLinks = new HashMap<>();
            quickLinks.put("enabled", "true".equals(systemConfigService.getConfigValue("footer.quick_links.enabled")));
            Map<String, Object> links = new HashMap<>();
            Map<String, Object> productsLink = new HashMap<>();
            productsLink.put("enabled", "true".equals(systemConfigService.getConfigValue("footer.quick_links.products.enabled")));
            productsLink.put("label", systemConfigService.getConfigValue("footer.quick_links.products.label"));
            links.put("products", productsLink);
            
            Map<String, Object> categoriesLink = new HashMap<>();
            categoriesLink.put("enabled", "true".equals(systemConfigService.getConfigValue("footer.quick_links.categories.enabled")));
            categoriesLink.put("label", systemConfigService.getConfigValue("footer.quick_links.categories.label"));
            links.put("categories", categoriesLink);
            
            Map<String, Object> aboutLink = new HashMap<>();
            aboutLink.put("enabled", "true".equals(systemConfigService.getConfigValue("footer.quick_links.about.enabled")));
            aboutLink.put("label", systemConfigService.getConfigValue("footer.quick_links.about.label"));
            links.put("about", aboutLink);
            
            Map<String, Object> contactLink = new HashMap<>();
            contactLink.put("enabled", "true".equals(systemConfigService.getConfigValue("footer.quick_links.contact.enabled")));
            contactLink.put("label", systemConfigService.getConfigValue("footer.quick_links.contact.label"));
            links.put("contact", contactLink);
            quickLinks.put("links", links);
            footerConfig.put("quickLinks", quickLinks);
            
            // Contact Information
            Map<String, Object> contact = new HashMap<>();
            contact.put("enabled", "true".equals(systemConfigService.getConfigValue("footer.contact.enabled")));
            Map<String, Object> contactDetails = new HashMap<>();
            Map<String, Object> address = new HashMap<>();
            address.put("enabled", "true".equals(systemConfigService.getConfigValue("footer.contact.address.enabled")));
            address.put("value", systemConfigService.getConfigValue("footer.contact.address.value"));
            contactDetails.put("address", address);
            
            Map<String, Object> phone = new HashMap<>();
            phone.put("enabled", "true".equals(systemConfigService.getConfigValue("footer.contact.phone.enabled")));
            phone.put("value", systemConfigService.getConfigValue("footer.contact.phone.value"));
            contactDetails.put("phone", phone);
            
            Map<String, Object> email = new HashMap<>();
            email.put("enabled", "true".equals(systemConfigService.getConfigValue("footer.contact.email.enabled")));
            email.put("value", systemConfigService.getConfigValue("footer.contact.email.value"));
            contactDetails.put("email", email);
            contact.put("details", contactDetails);
            footerConfig.put("contact", contact);
            
            // Social Media
            Map<String, Object> social = new HashMap<>();
            social.put("enabled", "true".equals(systemConfigService.getConfigValue("footer.social.enabled")));
            Map<String, Object> socialLinks = new HashMap<>();
            
            Map<String, Object> facebook = new HashMap<>();
            facebook.put("enabled", "true".equals(systemConfigService.getConfigValue("footer.social.facebook.enabled")));
            facebook.put("url", systemConfigService.getConfigValue("footer.social.facebook.url"));
            socialLinks.put("facebook", facebook);
            
            Map<String, Object> instagram = new HashMap<>();
            instagram.put("enabled", "true".equals(systemConfigService.getConfigValue("footer.social.instagram.enabled")));
            instagram.put("url", systemConfigService.getConfigValue("footer.social.instagram.url"));
            socialLinks.put("instagram", instagram);
            
            Map<String, Object> twitter = new HashMap<>();
            twitter.put("enabled", "true".equals(systemConfigService.getConfigValue("footer.social.twitter.enabled")));
            twitter.put("url", systemConfigService.getConfigValue("footer.social.twitter.url"));
            socialLinks.put("twitter", twitter);
            
            Map<String, Object> linkedin = new HashMap<>();
            linkedin.put("enabled", "true".equals(systemConfigService.getConfigValue("footer.social.linkedin.enabled")));
            linkedin.put("url", systemConfigService.getConfigValue("footer.social.linkedin.url"));
            socialLinks.put("linkedin", linkedin);
            
            Map<String, Object> youtube = new HashMap<>();
            youtube.put("enabled", "true".equals(systemConfigService.getConfigValue("footer.social.youtube.enabled")));
            youtube.put("url", systemConfigService.getConfigValue("footer.social.youtube.url"));
            socialLinks.put("youtube", youtube);
            
            Map<String, Object> pinterest = new HashMap<>();
            pinterest.put("enabled", "true".equals(systemConfigService.getConfigValue("footer.social.pinterest.enabled")));
            pinterest.put("url", systemConfigService.getConfigValue("footer.social.pinterest.url"));
            socialLinks.put("pinterest", pinterest);
            social.put("links", socialLinks);
            footerConfig.put("social", social);
            
            // Footer Bottom
            Map<String, Object> bottom = new HashMap<>();
            Map<String, Object> copyright = new HashMap<>();
            copyright.put("enabled", "true".equals(systemConfigService.getConfigValue("footer.copyright.enabled")));
            copyright.put("text", systemConfigService.getConfigValue("footer.copyright.text"));
            bottom.put("copyright", copyright);
            
            Map<String, Object> privacyPolicy = new HashMap<>();
            privacyPolicy.put("enabled", "true".equals(systemConfigService.getConfigValue("footer.privacy_policy.enabled")));
            privacyPolicy.put("label", systemConfigService.getConfigValue("footer.privacy_policy.label"));
            privacyPolicy.put("url", systemConfigService.getConfigValue("footer.privacy_policy.url"));
            bottom.put("privacyPolicy", privacyPolicy);
            
            Map<String, Object> termsOfService = new HashMap<>();
            termsOfService.put("enabled", "true".equals(systemConfigService.getConfigValue("footer.terms_of_service.enabled")));
            termsOfService.put("label", systemConfigService.getConfigValue("footer.terms_of_service.label"));
            termsOfService.put("url", systemConfigService.getConfigValue("footer.terms_of_service.url"));
            bottom.put("termsOfService", termsOfService);
            footerConfig.put("bottom", bottom);
            
            // Newsletter Subscription
            Map<String, Object> newsletter = new HashMap<>();
            newsletter.put("enabled", "true".equals(systemConfigService.getConfigValue("footer.newsletter.enabled")));
            newsletter.put("title", systemConfigService.getConfigValue("footer.newsletter.title"));
            newsletter.put("description", systemConfigService.getConfigValue("footer.newsletter.description"));
            newsletter.put("buttonText", systemConfigService.getConfigValue("footer.newsletter.button_text"));
            footerConfig.put("newsletter", newsletter);
            
            return ResponseEntity.ok(footerConfig);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load footer configuration");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Get top banner configuration
     * Accepts language parameter via Accept-Language header or query parameter
     */
    @GetMapping("/top-banner-config")
    @Operation(summary = "Get top banner configuration")
    public ResponseEntity<Map<String, Object>> getTopBannerConfig(
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            @RequestParam(value = "lang", required = false) String langParam) {
        try {
            Map<String, Object> bannerConfig = new HashMap<>();
            bannerConfig.put("enabled", "true".equals(systemConfigService.getConfigValue("site.top_banner.enabled")));
            
            // Get background color (default to black if not set)
            String backgroundColor = systemConfigService.getConfigValue("site.top_banner.background_color");
            if (backgroundColor == null || backgroundColor.trim().isEmpty()) {
                backgroundColor = "#000000"; // Default to black
            }
            bannerConfig.put("backgroundColor", backgroundColor);
            
            // Get font size (default to 0.875rem if not set)
            String fontSize = systemConfigService.getConfigValue("site.top_banner.font_size");
            if (fontSize == null || fontSize.trim().isEmpty()) {
                fontSize = "0.875rem"; // Default to slightly larger than current
            }
            bannerConfig.put("fontSize", fontSize);
            
            // Determine language: query param > Accept-Language header > default to en-US
            String language = langParam;
            if (language == null || language.trim().isEmpty()) {
                if (acceptLanguage != null && !acceptLanguage.trim().isEmpty()) {
                    // Parse Accept-Language header (e.g., "de-DE,de;q=0.9,en;q=0.8")
                    String[] parts = acceptLanguage.split(",");
                    if (parts.length > 0) {
                        String primaryLang = parts[0].trim().split(";")[0].trim();
                        // Map common language codes to our supported formats
                        if (primaryLang.startsWith("de")) {
                            language = "de-DE";
                        } else if (primaryLang.startsWith("ja") || primaryLang.startsWith("jp")) {
                            language = "ja-JP";
                        } else {
                            language = "en-US"; // Default
                        }
                    } else {
                        language = "en-US";
                    }
                } else {
                    language = "en-US"; // Default fallback
                }
            }
            
            // Normalize language code
            if (!language.equals("de-DE") && !language.equals("ja-JP")) {
                language = "en-US"; // Default to English if not supported
            }
            
            // Get legacy text for backward compatibility
            String text = systemConfigService.getConfigValue("site.top_banner.text");
            bannerConfig.put("text", text);
            
            // Get language-specific slides from database
            String slidesJson = systemConfigService.getConfigValue("site.top_banner.slides." + language);
            
            // Fallback to default slides if language-specific not found
            if (slidesJson == null || slidesJson.trim().isEmpty()) {
                slidesJson = systemConfigService.getConfigValue("site.top_banner.slides");
            }
            List<Map<String, String>> slides = new ArrayList<>();
            
            if (slidesJson != null && !slidesJson.trim().isEmpty()) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    slides = objectMapper.readValue(slidesJson, new TypeReference<List<Map<String, String>>>() {});
                } catch (Exception e) {
                    // If JSON parsing fails, create a default slide from the text
                    if (text != null && !text.trim().isEmpty()) {
                        Map<String, String> defaultSlide = new HashMap<>();
                        defaultSlide.put("text", text);
                        defaultSlide.put("link", "");
                        defaultSlide.put("linkText", "");
                        slides.add(defaultSlide);
                    }
                }
            } else if (text != null && !text.trim().isEmpty()) {
                // Fallback to single slide from text
                Map<String, String> defaultSlide = new HashMap<>();
                defaultSlide.put("text", text);
                defaultSlide.put("link", "");
                defaultSlide.put("linkText", "");
                slides.add(defaultSlide);
            }
            
            bannerConfig.put("slides", slides);
            
            return ResponseEntity.ok(bannerConfig);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load top banner configuration");
            errorResponse.put("enabled", false);
            errorResponse.put("text", "");
            errorResponse.put("slides", new ArrayList<>());
            return ResponseEntity.ok(errorResponse); // Return default values instead of error
        }
    }
    
    /**
     * Get popular searches for search modal
     * Accepts language parameter via Accept-Language header or query parameter
     */
    @GetMapping("/popular-searches")
    @Operation(summary = "Get popular search terms")
    public ResponseEntity<Map<String, Object>> getPopularSearches(
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            @RequestParam(value = "lang", required = false) String langParam) {
        try {
            // Determine language: query param > Accept-Language header > default to en-US
            String language = langParam;
            if (language == null || language.trim().isEmpty()) {
                if (acceptLanguage != null && !acceptLanguage.trim().isEmpty()) {
                    // Parse Accept-Language header (e.g., "de-DE,de;q=0.9,en;q=0.8")
                    String[] parts = acceptLanguage.split(",");
                    if (parts.length > 0) {
                        String primaryLang = parts[0].trim().split(";")[0].trim();
                        // Map common language codes to our supported formats
                        if (primaryLang.startsWith("de")) {
                            language = "de-DE";
                        } else if (primaryLang.startsWith("ja") || primaryLang.startsWith("jp")) {
                            language = "ja-JP";
                        } else {
                            language = "en-US"; // Default
                        }
                    } else {
                        language = "en-US";
                    }
                } else {
                    language = "en-US"; // Default fallback
                }
            }
            
            // Normalize language code
            if (!language.equals("de-DE") && !language.equals("ja-JP")) {
                language = "en-US"; // Default to English if not supported
            }
            
            // Get language-specific popular searches from database
            String popularSearchesStr = systemConfigService.getConfigValue("site.search.popular_searches." + language);
            
            // Fallback to default if language-specific not found
            if (popularSearchesStr == null || popularSearchesStr.trim().isEmpty()) {
                popularSearchesStr = systemConfigService.getConfigValue("site.search.popular_searches.en-US");
            }
            
            // Fallback to hardcoded defaults if still not found
            if (popularSearchesStr == null || popularSearchesStr.trim().isEmpty()) {
                popularSearchesStr = "Necklace,Ring,Earrings,Bracelet,Gold,Silver";
            }
            
            // Split comma-separated values
            List<String> popularSearches = new ArrayList<>();
            if (popularSearchesStr != null && !popularSearchesStr.trim().isEmpty()) {
                String[] terms = popularSearchesStr.split(",");
                for (String term : terms) {
                    String trimmed = term.trim();
                    if (!trimmed.isEmpty()) {
                        popularSearches.add(trimmed);
                    }
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("searches", popularSearches);
            response.put("language", language);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load popular searches");
            errorResponse.put("searches", Arrays.asList("Necklace", "Ring", "Earrings", "Bracelet", "Gold", "Silver"));
            return ResponseEntity.ok(errorResponse); // Return default values instead of error
        }
    }
    
    /**
     * Get site configuration (page title and navbar name)
     */
    @GetMapping("/site-config")
    @Operation(summary = "Get site configuration")
    public ResponseEntity<Map<String, Object>> getSiteConfig() {
        try {
            Map<String, Object> siteConfig = new HashMap<>();
            siteConfig.put("pageTitle", systemConfigService.getConfigValue("site.page_title"));
            siteConfig.put("navbarName", systemConfigService.getConfigValue("site.navbar_name"));
            return ResponseEntity.ok(siteConfig);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load site configuration");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Get shipping countries configuration
     */
    @GetMapping("/shipping-countries")
    @Operation(summary = "Get available shipping countries")
    public ResponseEntity<Map<String, Object>> getShippingCountries() {
        try {
            Map<String, Object> shippingConfig = new HashMap<>();
            String countriesStr = systemConfigService.getConfigValue("shipping.countries.enabled");
            String defaultCountry = systemConfigService.getConfigValue("shipping.countries.default");
            
            List<String> countries = countriesStr != null && !countriesStr.isEmpty()
                ? Arrays.asList(countriesStr.split(","))
                    .stream()
                    .map(String::trim)
                    .collect(java.util.stream.Collectors.toList())
                : new ArrayList<>();
            
            shippingConfig.put("countries", countries);
            shippingConfig.put("default", defaultCountry != null ? defaultCountry : "");
            
            return ResponseEntity.ok(shippingConfig);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load shipping countries");
            errorResponse.put("countries", new ArrayList<>());
            errorResponse.put("default", "");
            return ResponseEntity.ok(errorResponse); // Return empty list instead of error
        }
    }
    
    /**
     * Get category description template
     */
    @GetMapping("/category-description-template/{categoryName}")
    @Operation(summary = "Get description template for a specific category")
    public ResponseEntity<Map<String, String>> getCategoryDescriptionTemplate(@PathVariable String categoryName) {
        try {
            String templateKey = "category.description_template." + categoryName;
            String template = systemConfigService.getConfigValue(templateKey);
            
            Map<String, String> response = new HashMap<>();
            if (template != null && !template.isEmpty()) {
                response.put("template", template);
                response.put("category", categoryName);
            } else {
                // Return null template if not configured
                response.put("template", null);
                response.put("category", categoryName);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("template", null);
            errorResponse.put("error", "Failed to load category description template");
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * Get free shipping configuration
     */
    @GetMapping("/free-shipping-config")
    @Operation(summary = "Get free shipping configuration")
    public ResponseEntity<Map<String, Object>> getFreeShippingConfig() {
        try {
            Map<String, Object> freeShippingConfig = new HashMap<>();
            
            String enabledStr = systemConfigService.getConfigValue("free_shipping.enabled");
            String thresholdStr = systemConfigService.getConfigValue("free_shipping.threshold");
            
            boolean enabled = enabledStr != null && enabledStr.equalsIgnoreCase("true");
            double threshold = thresholdStr != null ? Double.parseDouble(thresholdStr) : 150.0;
            
            freeShippingConfig.put("enabled", enabled);
            freeShippingConfig.put("threshold", threshold);
            
            return ResponseEntity.ok(freeShippingConfig);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load free shipping configuration");
            errorResponse.put("enabled", false);
            errorResponse.put("threshold", 150.0);
            return ResponseEntity.ok(errorResponse); // Return default values instead of error
        }
    }
    
    /**
     * Get translated special offer descriptions
     * Accepts language parameter via Accept-Language header or query parameter
     */
    @GetMapping("/special-offer-descriptions")
    @Operation(summary = "Get translated special offer descriptions")
    public ResponseEntity<Map<String, Object>> getSpecialOfferDescriptions(
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            @RequestParam(value = "lang", required = false) String langParam) {
        try {
            // Determine language: query param > Accept-Language header > default to en-US
            String language = langParam;
            if (language == null || language.trim().isEmpty()) {
                if (acceptLanguage != null && !acceptLanguage.trim().isEmpty()) {
                    // Parse Accept-Language header (e.g., "de-DE,de;q=0.9,en;q=0.8")
                    String[] parts = acceptLanguage.split(",");
                    if (parts.length > 0) {
                        String primaryLang = parts[0].trim().split(";")[0].trim();
                        // Map common language codes to our supported formats
                        if (primaryLang.startsWith("de")) {
                            language = "de-DE";
                        } else if (primaryLang.startsWith("ja") || primaryLang.startsWith("jp")) {
                            language = "ja-JP";
                        } else {
                            language = "en-US"; // Default
                        }
                    } else {
                        language = "en-US";
                    }
                } else {
                    language = "en-US"; // Default fallback
                }
            }
            
            // Normalize language code
            if (!language.equals("de-DE") && !language.equals("ja-JP")) {
                language = "en-US"; // Default to English if not supported
            }
            
            // Build translation map for common special offer descriptions
            Map<String, String> translations = new HashMap<>();
            
            // Common special offer descriptions
            String[] descriptions = {
                "Limited Time Offer", "Free Shipping", "Flash Sale", 
                "Holiday Special", "Clearance", "New Arrival", "Best Seller"
            };
            
            for (String desc : descriptions) {
                String key = desc.toLowerCase().replace(" ", "_");
                String configKey = "special_offer." + key + "." + language;
                String translated = systemConfigService.getConfigValue(configKey);
                
                // Fallback to English if translation not found
                if (translated == null || translated.trim().isEmpty()) {
                    translated = systemConfigService.getConfigValue("special_offer." + key + ".en-US");
                }
                
                // Final fallback to original name
                if (translated == null || translated.trim().isEmpty()) {
                    translated = desc;
                }
                
                translations.put(desc, translated);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("translations", translations);
            response.put("language", language);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load special offer descriptions");
            errorResponse.put("translations", new HashMap<>());
            return ResponseEntity.ok(errorResponse); // Return default values instead of error
        }
    }
    
    /**
     * Get system config value by key
     */
    @GetMapping("/system-config/{configKey}")
    @Operation(summary = "Get system configuration value by key")
    public ResponseEntity<Map<String, Object>> getSystemConfigValue(@PathVariable String configKey) {
        try {
            Map<String, Object> response = new HashMap<>();
            String value = systemConfigService.getConfigValue(configKey);
            response.put("key", configKey);
            response.put("value", value);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("key", configKey);
            errorResponse.put("value", null);
            errorResponse.put("error", "Failed to load configuration value");
            return ResponseEntity.ok(errorResponse);
        }
    }
}


