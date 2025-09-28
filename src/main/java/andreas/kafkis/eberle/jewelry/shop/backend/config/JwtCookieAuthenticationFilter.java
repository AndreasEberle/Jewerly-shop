package andreas.kafkis.eberle.jewelry.shop.backend.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import andreas.kafkis.eberle.jewelry.shop.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtCookieAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // Skip authentication for public endpoints
        String requestURI = request.getRequestURI();
        if (isPublicEndpoint(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Extract JWT token from HTTP-only cookie
        String jwtToken = extractTokenFromCookie(request);
        
        if (jwtToken != null) {
            try {
                String userEmail = jwtService.extractUsername(jwtToken);
                
                if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                    
                    if (jwtService.isTokenValid(jwtToken, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        
                        log.debug("JWT cookie authentication successful for user: {}", userEmail);
                    }
                }
            } catch (Exception e) {
                log.debug("JWT cookie authentication failed: {}", e.getMessage());
                // Clear invalid cookie
                clearJwtCookies(response);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extract JWT token from HTTP-only cookie
     */
    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    
    /**
     * Clear JWT cookies when token is invalid
     */
    private void clearJwtCookies(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("jwt_token", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        response.addCookie(accessTokenCookie);
        
        Cookie refreshTokenCookie = new Cookie("jwt_refresh_token", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(refreshTokenCookie);
    }
    
    /**
     * Check if endpoint is public (doesn't require authentication)
     */
    private boolean isPublicEndpoint(String requestURI) {
        return requestURI.startsWith("/api/auth/login") ||
               requestURI.startsWith("/api/auth/register") ||
               requestURI.startsWith("/api/auth/oauth2/") ||
               requestURI.startsWith("/api/auth/verify-2fa") ||
               requestURI.startsWith("/api/auth/session/") ||
               requestURI.startsWith("/api/auth/jwt/") ||
               // Removed /api/auth/debug/ from public endpoints so JWT filter processes it
               requestURI.startsWith("/api/products") ||
               requestURI.startsWith("/static/") ||
               requestURI.startsWith("/oauth2/") ||
               requestURI.startsWith("/login/oauth2/") ||
               requestURI.equals("/error") ||
               requestURI.equals("/favicon.ico");
    }
}
