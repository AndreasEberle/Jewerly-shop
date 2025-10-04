package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.util.Arrays;
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

    private static final String[] SUPPORTED_LANGUAGES = {
        "de-DE", // German (Germany) - Default
        "en-US", // English (United States)
        "ja-JP", // Japanese (Japan)
        "fr-FR", // French (France)
        "it-IT"  // Italian (Italy)
    };

    public String[] getSupportedLanguages() {
        return Arrays.copyOf(SUPPORTED_LANGUAGES, SUPPORTED_LANGUAGES.length);
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
        return Arrays.asList(SUPPORTED_LANGUAGES).contains(language);
    }
}
