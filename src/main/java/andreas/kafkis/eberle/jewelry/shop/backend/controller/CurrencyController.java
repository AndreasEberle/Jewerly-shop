package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

@RestController
@RequestMapping("/api/currency")
@Tag(name = "Currency", description = "Currency conversion and preferences")
public class CurrencyController {

    @Autowired
    private CurrencyService currencyService;

    @GetMapping("/supported")
    @Operation(summary = "Get supported currencies")
    public ResponseEntity<String[]> getSupportedCurrencies() {
        return ResponseEntity.ok(currencyService.getSupportedCurrencies());
    }

    @GetMapping("/convert")
    @Operation(summary = "Convert price between currencies")
    public ResponseEntity<ConvertResponse> convertPrice(
            @Parameter(description = "Price to convert") @RequestParam BigDecimal price,
            @Parameter(description = "Source currency") @RequestParam String fromCurrency,
            @Parameter(description = "Target currency") @RequestParam String toCurrency) {
        
        BigDecimal convertedPrice = currencyService.convertPrice(price, fromCurrency, toCurrency);
        
        ConvertResponse response = ConvertResponse.builder()
                .originalPrice(price)
                .originalCurrency(fromCurrency)
                .convertedPrice(convertedPrice)
                .targetCurrency(toCurrency)
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/preference")
    @Operation(summary = "Get user's preferred currency")
    public ResponseEntity<CurrencyPreferenceResponse> getUserCurrencyPreference(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.ok(CurrencyPreferenceResponse.builder()
                    .currency("CHF")
                    .isDefault(true)
                    .build());
        }

        User user = (User) authentication.getPrincipal();
        String preferredCurrency = currencyService.getUserPreferredCurrency(user, "CHF");
        
        return ResponseEntity.ok(CurrencyPreferenceResponse.builder()
                .currency(preferredCurrency)
                .isDefault(preferredCurrency.equals("CHF"))
                .build());
    }

    @PostMapping("/preference")
    @Operation(summary = "Set user's preferred currency")
    public ResponseEntity<CurrencyPreferenceResponse> setUserCurrencyPreference(
            @RequestBody SetCurrencyPreferenceRequest request,
            Authentication authentication) {
        
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.badRequest().build();
        }

        User user = (User) authentication.getPrincipal();
        currencyService.setUserPreferredCurrency(user.getId(), request.getCurrency());
        
        return ResponseEntity.ok(CurrencyPreferenceResponse.builder()
                .currency(request.getCurrency())
                .isDefault(request.getCurrency().equals("CHF"))
                .build());
    }

    @Data
    @lombok.Builder
    public static class ConvertResponse {
        private BigDecimal originalPrice;
        private String originalCurrency;
        private BigDecimal convertedPrice;
        private String targetCurrency;
    }

    @Data
    @lombok.Builder
    public static class CurrencyPreferenceResponse {
        private String currency;
        private boolean isDefault;
    }

    @Data
    public static class SetCurrencyPreferenceRequest {
        private String currency;
    }
}
