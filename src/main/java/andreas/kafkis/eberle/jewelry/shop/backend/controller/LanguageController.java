package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.service.JwtService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.LanguageService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;

@RestController
@RequestMapping("/api/language")
@Tag(name = "Language", description = "Language preferences and supported languages")
public class LanguageController {

    @Autowired
    private LanguageService languageService;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/supported")
    @Operation(summary = "Get supported languages")
    public ResponseEntity<String[]> getSupportedLanguages() {
        return ResponseEntity.ok(languageService.getSupportedLanguages());
    }

    @GetMapping("/preference")
    @Operation(summary = "Get user's preferred language")
    public ResponseEntity<LanguagePreferenceResponse> getUserLanguagePreference(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request) {
        
        User user = getAuthenticatedUser(authHeader, request);
        if (user == null) {
            return ResponseEntity.ok(LanguagePreferenceResponse.builder()
                    .language("de-DE")
                    .isDefault(true)
                    .build());
        }

        String preferredLanguage = languageService.getUserPreferredLanguage(user, "de-DE");
        
        return ResponseEntity.ok(LanguagePreferenceResponse.builder()
                .language(preferredLanguage)
                .isDefault(preferredLanguage.equals("de-DE"))
                .build());
    }

    @PostMapping("/preference")
    @Operation(summary = "Set user's preferred language")
    public ResponseEntity<LanguagePreferenceResponse> setUserLanguagePreference(
            @RequestBody SetLanguagePreferenceRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest httpRequest) {
        
        User user = getAuthenticatedUser(authHeader, httpRequest);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        languageService.setUserPreferredLanguage(user.getId(), request.getLanguage());
        
        return ResponseEntity.ok(LanguagePreferenceResponse.builder()
                .language(request.getLanguage())
                .isDefault(request.getLanguage().equals("de-DE"))
                .build());
    }

    @Data
    @lombok.Builder
    public static class LanguagePreferenceResponse {
        private String language;
        private boolean isDefault;
    }

    @Data
    public static class SetLanguagePreferenceRequest {
        private String language;
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
