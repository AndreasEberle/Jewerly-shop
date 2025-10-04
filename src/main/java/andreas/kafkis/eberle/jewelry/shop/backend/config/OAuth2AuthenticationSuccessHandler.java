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

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.oauth2.authorized-redirect-uris:http://localhost:3000/auth/callback}")
    private String redirectUri;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect.");
            return;
        }

        try {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String email = oauth2User.getAttribute("email");
            
            log.info("OAuth2 login attempt for email: {}", email);
            
            // Process OAuth2 login and get JWT tokens
            AuthenticationResponse authResponse = oauth2Service.processOAuth2Login(oauth2User);
            
            // Check if 2FA is required
            if (authResponse.getRequires2FA() != null && authResponse.getRequires2FA()) {
                log.info("🔐 2FA required for admin user: {}", email);
                // Redirect to 2FA verification page
                String twoFactorUrl = "http://localhost:8080/static/oauth2-2fa-required.html";
                String targetUrl = UriComponentsBuilder.fromUriString(twoFactorUrl)
                        .queryParam("email", URLEncoder.encode(email, StandardCharsets.UTF_8))
                        .build().toUriString();
                getRedirectStrategy().sendRedirect(request, response, targetUrl);
                return;
            }
            
            // Check if user has admin role and log accordingly
            boolean isAdmin = authResponse.getUser().getRoles().contains("ADMIN");
            if (isAdmin) {
                log.info("✅ OAuth2 ADMIN login successful for email: {} - User has ADMIN role", email);
                log.info("Admin user can now access: Swagger UI, 2FA setup, Health checks, Admin endpoints");
            } else {
                log.info("✅ OAuth2 CUSTOMER login successful for email: {} - User has CUSTOMER role", email);
            }
            
            // Set HTTP-only cookies for JWT tokens
            if (authResponse.getAccessToken() != null) {
                setHttpOnlyCookie(response, "jwt_token", authResponse.getAccessToken(), 86400); // 24 hours
            }
            if (authResponse.getRefreshToken() != null) {
                setHttpOnlyCookie(response, "jwt_refresh_token", authResponse.getRefreshToken(), 604800); // 7 days
            }
            
            // Get redirect URL from saved request or default to home
            String redirectUrl = getRedirectUrl(request);
            
            // Redirect to frontend with tokens in URL (for frontend to handle)
            String userJson = objectMapper.writeValueAsString(authResponse.getUser());
            log.info("OAuth2 Success Handler - Email from OAuth: {}", email);
            log.info("OAuth2 Success Handler - User from authResponse: {}", authResponse.getUser().getEmail());
            log.info("OAuth2 Success Handler - User JSON for redirect: {}", userJson);
            
            String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/callback")
                    .queryParam("token", authResponse.getAccessToken())
                    .queryParam("refreshToken", authResponse.getRefreshToken())
                    .queryParam("user", URLEncoder.encode(userJson, StandardCharsets.UTF_8))
                    .queryParam("isAdmin", isAdmin)
                    .queryParam("redirect", URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8))
                    .build().toUriString();

            log.info("Redirecting to frontend: {}", targetUrl);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            
            // Option 2: Return JSON response (for API-only approach)
            // response.setContentType("application/json");
            // response.setCharacterEncoding("UTF-8");
            // response.getWriter().write(objectMapper.writeValueAsString(authResponse));
            
        } catch (Exception e) {
            log.error("Error processing OAuth2 authentication: " + e.getMessage(), e);
            log.error("Exception type: " + e.getClass().getSimpleName());
            if (e.getCause() != null) {
                log.error("Caused by: " + e.getCause().getMessage());
            }
            
            // Redirect to error page
            String errorUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("error", "authentication_failed")
                    .queryParam("message", e.getMessage())
                    .build().toUriString();
                    
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }
    
    /**
     * Set HTTP-only cookie for JWT token storage
     */
    private void setHttpOnlyCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        response.addCookie(cookie);
        log.debug("Set HTTP-only cookie: {} with maxAge: {} seconds", name, maxAgeSeconds);
    }
    
    private String getRedirectUrl(HttpServletRequest request) {
        // Try to get redirect URL from session or request
        String redirectUrl = (String) request.getSession().getAttribute("redirectUrl");
        if (redirectUrl == null) {
            // Check for redirect parameter in the request
            redirectUrl = request.getParameter("redirect");
        }
        if (redirectUrl == null) {
            // Default to home page
            redirectUrl = "/";
        }
        return redirectUrl;
    }
}
