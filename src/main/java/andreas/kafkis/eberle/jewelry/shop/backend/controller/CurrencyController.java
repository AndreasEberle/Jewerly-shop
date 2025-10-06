package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.service.CurrencyService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.JwtService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;

@RestController
@RequestMapping("/api/currency")
@Tag(name = "Currency", description = "Currency conversion and preferences")
public class CurrencyController {

    @Autowired
    private CurrencyService currencyService;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private UserService userService;

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
    public ResponseEntity<CurrencyPreferenceResponse> getUserCurrencyPreference(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request) {
        
        User user = getAuthenticatedUser(authHeader, request);
        if (user == null) {
            return ResponseEntity.ok(CurrencyPreferenceResponse.builder()
                    .currency("CHF")
                    .isDefault(true)
                    .build());
        }

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
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest httpRequest) {
        
        User user = getAuthenticatedUser(authHeader, httpRequest);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

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
    
    /**
     * Helper method to get authenticated user from either Authorization header or HTTP-only cookie
     */
    private User getAuthenticatedUser(String authHeader, HttpServletRequest request) {
        try {
            String token = null;
            
            // Try to get token from Authorization header first (for email/password login)
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            } else {
                // Try to get token from HTTP-only cookie (for OAuth login)
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("jwt_token".equals(cookie.getName())) {
                            token = cookie.getValue();
                            break;
                        }
                    }
                }
            }
            
            if (token == null) {
                return null;
            }

            String userEmail = jwtService.extractUsername(token);
            if (userEmail != null) {
                return userService.findByEmail(userEmail);
                        
            }
        } catch (Exception e) {
            // Log error and return null
            return null;
        }
        
        return null;
    }
}
