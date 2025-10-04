package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.CurrencyRate;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.UserPreferences;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.CurrencyRateRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.UserPreferencesRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CurrencyService {

    @Autowired
    private CurrencyRateRepository currencyRateRepository;

    @Autowired
    private UserPreferencesRepository userPreferencesRepository;

    @Autowired
    private MarkupConfigurationService markupService;

    private static final String DEFAULT_CURRENCY = "CHF";

    /**
     * Get user's preferred currency, with fallback to location-based detection
     */
    public String getUserPreferredCurrency(User user, String detectedCurrency) {
        if (user == null) {
            return isValidCurrency(detectedCurrency) ? detectedCurrency : DEFAULT_CURRENCY;
        }

        // Check user's saved preference first
        Optional<UserPreferences> preferences = userPreferencesRepository.findByUserId(user.getId());
        if (preferences.isPresent()) {
            return preferences.get().getPreferredCurrency();
        }

        // Fallback to detected currency or default
        return isValidCurrency(detectedCurrency) ? detectedCurrency : DEFAULT_CURRENCY;
    }

    /**
     * Set user's preferred currency
     */
    @Transactional
    public void setUserPreferredCurrency(UUID userId, String currency) {
        if (!isValidCurrency(currency)) {
            throw new IllegalArgumentException("Invalid currency: " + currency);
        }

        Optional<UserPreferences> existing = userPreferencesRepository.findByUserId(userId);
        if (existing.isPresent()) {
            existing.get().setPreferredCurrency(currency);
            userPreferencesRepository.save(existing.get());
        } else {
            UserPreferences preferences = UserPreferences.builder()
                    .userId(userId)  // Set userId directly
                    .user(User.builder().id(userId).build())
                    .preferredCurrency(currency)
                    .preferredLanguage("de-DE")  // Set default language
                    .build();
            userPreferencesRepository.save(preferences);
        }
    }

    /**
     * Convert price from one currency to another
     */
    public BigDecimal convertPrice(BigDecimal price, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return price;
        }

        // Try direct conversion
        Optional<CurrencyRate> directRate = currencyRateRepository.findByFromCurrencyAndToCurrency(fromCurrency, toCurrency);
        if (directRate.isPresent()) {
            return applyConversion(price, directRate.get());
        }

        // Try reverse conversion
        Optional<CurrencyRate> reverseRate = currencyRateRepository.findByFromCurrencyAndToCurrency(toCurrency, fromCurrency);
        if (reverseRate.isPresent()) {
            BigDecimal reverseRateValue = BigDecimal.ONE.divide(reverseRate.get().getRate(), 8, RoundingMode.HALF_UP);
            BigDecimal markupMultiplier = BigDecimal.ONE.add(reverseRate.get().getMarkupPercentage().divide(BigDecimal.valueOf(100)));
            return price.multiply(reverseRateValue).multiply(markupMultiplier).setScale(2, RoundingMode.HALF_UP);
        }

        // Fallback: Convert through CHF
        if (!fromCurrency.equals(DEFAULT_CURRENCY) && !toCurrency.equals(DEFAULT_CURRENCY)) {
            BigDecimal toChf = convertPrice(price, fromCurrency, DEFAULT_CURRENCY);
            return convertPrice(toChf, DEFAULT_CURRENCY, toCurrency);
        }

        log.warn("No conversion rate found from {} to {}", fromCurrency, toCurrency);
        return price; // Return original price if no conversion found
    }

    /**
     * Apply conversion with markup
     */
    private BigDecimal applyConversion(BigDecimal price, CurrencyRate rate) {
        // Get markup from configuration service instead of rate
        BigDecimal markupPercentage = markupService.getMarkupPercentage(rate.getToCurrency());
        BigDecimal markupMultiplier = BigDecimal.ONE.add(markupPercentage.divide(BigDecimal.valueOf(100)));
        return price.multiply(rate.getRate()).multiply(markupMultiplier).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Check if currency is valid/supported
     */
    private boolean isValidCurrency(String currency) {
        return currency != null && currency.length() == 3 && 
               (currency.equals("CHF") || currency.equals("EUR") || currency.equals("USD") || 
                currency.equals("JPY") || currency.equals("GBP") || currency.equals("CAD") || 
                currency.equals("AUD"));
    }

    /**
     * Get supported currencies
     */
    public String[] getSupportedCurrencies() {
        return new String[]{"CHF", "EUR", "JPY"};
    }

    /**
     * Update exchange rates (to be called by scheduled task)
     */
    @Transactional
    public void updateExchangeRates() {
        // This would call external API and update rates
        // For now, we'll keep the static rates from migration
        log.info("Exchange rates update - using static rates for now");
    }
}
