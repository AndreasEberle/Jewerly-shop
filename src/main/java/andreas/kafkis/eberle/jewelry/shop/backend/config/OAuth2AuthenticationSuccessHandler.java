package andreas.kafkis.eberle.jewelry.shop.backend.config;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.AuthenticationResponse;
import andreas.kafkis.eberle.jewelry.shop.backend.service.OAuth2Service;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private OAuth2Service oauth2Service;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        
        try {
            // Process OAuth2 login
            AuthenticationResponse authResponse = oauth2Service.processOAuth2Login(oauth2User);
            
            if (authResponse.getError() != null) {
                // Handle error case
                String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/login")
                        .queryParam("error", URLEncoder.encode(authResponse.getError(), StandardCharsets.UTF_8))
                        .build()
                        .toUriString();
                getRedirectStrategy().sendRedirect(request, response, errorUrl);
                return;
            }
            
            if (authResponse.getRequires2FA() != null && authResponse.getRequires2FA()) {
                // Handle 2FA required case
                String userInfoJson = objectMapper.writeValueAsString(authResponse.getUser());
                String userInfoEncoded = URLEncoder.encode(userInfoJson, StandardCharsets.UTF_8);
                
                String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/2fa")
                        .queryParam("user", userInfoEncoded)
                        .build()
                        .toUriString();
                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
                return;
            }
            
            // Set JWT tokens in HTTP-only cookies for OAuth2 users
            jakarta.servlet.http.Cookie accessTokenCookie = new jakarta.servlet.http.Cookie("jwt_token", authResponse.getAccessToken());
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(false); // Set to true in production with HTTPS
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(86400); // 24 hours
            response.addCookie(accessTokenCookie);
            
            if (authResponse.getRefreshToken() != null) {
                jakarta.servlet.http.Cookie refreshTokenCookie = new jakarta.servlet.http.Cookie("jwt_refresh_token", authResponse.getRefreshToken());
                refreshTokenCookie.setHttpOnly(true);
                refreshTokenCookie.setSecure(false); // Set to true in production with HTTPS
                refreshTokenCookie.setPath("/");
                refreshTokenCookie.setMaxAge(604800); // 7 days
                response.addCookie(refreshTokenCookie);
            }
            
            // Handle successful login - send individual parameters that frontend expects
            String userInfoJson = objectMapper.writeValueAsString(authResponse.getUser());
            String userInfoEncoded = URLEncoder.encode(userInfoJson, StandardCharsets.UTF_8);
            
            String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/callback")
                    .queryParam("token", authResponse.getAccessToken())
                    .queryParam("refreshToken", authResponse.getRefreshToken())
                    .queryParam("user", userInfoEncoded)
                    .queryParam("isAdmin", authResponse.getUser().getRoles().contains("ADMIN") ? "true" : "false")
                    .build()
                    .toUriString();
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
            
        } catch (Exception e) {
            log.error("Error processing OAuth2 authentication", e);
            String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/login")
                    .queryParam("error", URLEncoder.encode("Authentication failed. Please try again.", StandardCharsets.UTF_8))
                    .build()
                    .toUriString();
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }
}


