package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.UserPreferences;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.UserPreferencesRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LanguageService {

    @Autowired
    private UserPreferencesRepository userPreferencesRepository;
    
    @Autowired
    private SystemConfigService systemConfigService;

    private static final String[] ALL_POSSIBLE_LANGUAGES = {
        "de-DE", // German (Germany) - Default
        "en-US", // English (United States)
        "ja-JP", // Japanese (Japan)
        "fr-FR", // French (France)
        "it-IT"  // Italian (Italy)
    };

    public String[] getSupportedLanguages() {
        List<String> enabledLanguages = new ArrayList<>();
        
        // Check each language's enabled status from system_config
        for (String lang : ALL_POSSIBLE_LANGUAGES) {
            String configKey = "language." + lang + ".enabled";
            String enabled = systemConfigService.getConfigValue(configKey);
            
            // If config exists and is "true", include the language
            // If config doesn't exist, default behavior: de-DE, en-US, ja-JP are enabled by default
            if (enabled != null && "true".equalsIgnoreCase(enabled)) {
                enabledLanguages.add(lang);
            } else if (enabled == null) {
                // If config doesn't exist, use default: enable de-DE, en-US, ja-JP
                if (Arrays.asList("de-DE", "en-US", "ja-JP").contains(lang)) {
                    enabledLanguages.add(lang);
                }
            }
        }
        
        // Fallback: if no languages are enabled, return default enabled ones
        if (enabledLanguages.isEmpty()) {
            return new String[]{"de-DE", "en-US", "ja-JP"};
        }
        
        return enabledLanguages.toArray(new String[0]);
    }

    public String getUserPreferredLanguage(User user, String defaultLanguage) {
        try {
            UserPreferences preferences = userPreferencesRepository.findByUserId(user.getId())
                    .orElse(null);
            
            if (preferences != null && preferences.getPreferredLanguage() != null) {
                return preferences.getPreferredLanguage();
            }
        } catch (Exception e) {
            log.warn("Failed to get language preference for user {}: {}", user.getId(), e.getMessage());
        }
        
        return defaultLanguage;
    }

    @Transactional
    public void setUserPreferredLanguage(UUID userId, String language) {
        if (!isValidLanguage(language)) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }

        try {
            UserPreferences preferences = userPreferencesRepository.findByUserId(userId)
                    .orElse(new UserPreferences());
            
            if (preferences.getId() == null) {
                preferences.setUserId(userId);
            }
            
            preferences.setPreferredLanguage(language);
            userPreferencesRepository.save(preferences);
            
            log.info("Set language preference for user {} to {}", userId, language);
        } catch (Exception e) {
            log.error("Failed to set language preference for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to save language preference", e);
        }
    }

    private boolean isValidLanguage(String language) {
        String[] supported = getSupportedLanguages();
        return Arrays.asList(supported).contains(language);
    }
}
