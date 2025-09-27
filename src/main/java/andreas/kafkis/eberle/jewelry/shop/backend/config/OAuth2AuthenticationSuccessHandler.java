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

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private OAuth2Service oauth2Service;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.oauth2.authorized-redirect-uris:http://localhost:3000/auth/callback}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect.");
            return;
        }

        try {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            
            // Process OAuth2 login and get JWT tokens
            AuthenticationResponse authResponse = oauth2Service.processOAuth2Login(oauth2User);
            
            // Redirect to our custom success page with tokens
            String successUrl = "http://localhost:8080/api/auth/oauth2/success";
            String targetUrl = UriComponentsBuilder.fromUriString(successUrl)
                    .queryParam("token", authResponse.getAccessToken())
                    .queryParam("refreshToken", authResponse.getRefreshToken())
                    .queryParam("user", URLEncoder.encode(objectMapper.writeValueAsString(authResponse.getUser()), StandardCharsets.UTF_8))
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            
            // Option 2: Return JSON response (for API-only approach)
            // response.setContentType("application/json");
            // response.setCharacterEncoding("UTF-8");
            // response.getWriter().write(objectMapper.writeValueAsString(authResponse));
            
        } catch (Exception e) {
            logger.error("Error processing OAuth2 authentication: " + e.getMessage(), e);
            logger.error("Exception type: " + e.getClass().getSimpleName());
            if (e.getCause() != null) {
                logger.error("Caused by: " + e.getCause().getMessage());
            }
            
            // Redirect to error page
            String errorUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("error", "authentication_failed")
                    .queryParam("message", e.getMessage())
                    .build().toUriString();
                    
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }
}
