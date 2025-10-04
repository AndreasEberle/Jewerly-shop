package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.SystemConfig;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.SystemConfigRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MarkupConfigurationService {

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    // Default markup values from application.properties
    @Value("${app.currency.markup.default:5.0}")
    private BigDecimal defaultMarkup;

    @Value("${app.currency.markup.eur:3.0}")
    private BigDecimal eurMarkup;

    @Value("${app.currency.markup.jpy:6.0}")
    private BigDecimal jpyMarkup;

    /**
     * Get markup percentage for a specific currency
     */
    public BigDecimal getMarkupPercentage(String currency) {
        // Try to get from database first
        Optional<SystemConfig> config = systemConfigRepository.findByConfigKey("currency.markup." + currency.toLowerCase());
        if (config.isPresent()) {
            return new BigDecimal(config.get().getConfigValue());
        }

        // Fallback to application properties
        return getDefaultMarkupForCurrency(currency);
    }

    /**
     * Set markup percentage for a specific currency
     */
    @Transactional
    public void setMarkupPercentage(String currency, BigDecimal markup) {
        if (markup.compareTo(BigDecimal.ZERO) < 0 || markup.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Markup must be between 0 and 100");
        }

        String configKey = "currency.markup." + currency.toLowerCase();
        Optional<SystemConfig> existing = systemConfigRepository.findByConfigKey(configKey);
        
        if (existing.isPresent()) {
            existing.get().setConfigValue(markup.toString());
            systemConfigRepository.save(existing.get());
        } else {
            SystemConfig config = SystemConfig.builder()
                    .configKey(configKey)
                    .configValue(markup.toString())
                    .description("Markup percentage for " + currency + " conversions")
                    .build();
            systemConfigRepository.save(config);
        }

        log.info("Updated markup for {} to {}%", currency, markup);
    }

    /**
     * Get all markup configurations
     */
    public Map<String, BigDecimal> getAllMarkupConfigurations() {
        Map<String, BigDecimal> markups = new HashMap<>();
        
        String[] currencies = {"CHF", "EUR", "JPY"};
        for (String currency : currencies) {
            markups.put(currency, getMarkupPercentage(currency));
        }
        
        return markups;
    }

    /**
     * Reset all markups to default values
     */
    @Transactional
    public void resetToDefaults() {
        setMarkupPercentage("CHF", BigDecimal.ZERO);
        setMarkupPercentage("EUR", eurMarkup);
        setMarkupPercentage("JPY", jpyMarkup);
        
        log.info("Reset all markup configurations to defaults");
    }

    private BigDecimal getDefaultMarkupForCurrency(String currency) {
        return switch (currency.toUpperCase()) {
            case "CHF" -> BigDecimal.ZERO;
            case "EUR" -> eurMarkup;
            case "JPY" -> jpyMarkup;
            default -> defaultMarkup;
        };
    }
}
