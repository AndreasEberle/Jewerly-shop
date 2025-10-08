package andreas.kafkis.eberle.jewelry.shop.backend.config;

import java.io.IOException;

import org.springframework.lang.NonNull;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // Skip JWT processing for public endpoints
        if (isPublicEndpoint(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        System.out.println("JWT Filter: Processing request to " + request.getServletPath());
        System.out.println("JWT Filter: Authorization header: " + (authHeader != null ? authHeader.substring(0, Math.min(20, authHeader.length())) + "..." : "null"));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("JWT Filter: No valid Authorization header, skipping JWT processing");
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        
        try {
            userEmail = jwtService.extractUsername(jwt);
            System.out.println("JWT Filter: Extracted user email: " + userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                System.out.println("JWT Filter: Loading user details for: " + userEmail);
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                
                System.out.println("JWT Filter: User details loaded: " + (userDetails != null ? userDetails.getUsername() : "null"));
                System.out.println("JWT Filter: Token valid: " + jwtService.isTokenValid(jwt, userDetails));
                
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    System.out.println("JWT Filter: Setting authentication for user: " + userEmail);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("JWT Filter: Authentication set successfully");
                } else {
                    System.out.println("JWT Filter: Token is invalid for user: " + userEmail);
                }
            } else {
                System.out.println("JWT Filter: User email is null or authentication already exists");
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println("JWT Filter: Token expired - " + e.getMessage());
            // Don't set authentication, let the request continue without authentication
            // The client should handle this by refreshing the token or redirecting to login
        } catch (io.jsonwebtoken.JwtException e) {
            System.out.println("JWT Filter: Invalid JWT token - " + e.getMessage());
            // Don't set authentication, let the request continue without authentication
        } catch (Exception e) {
            System.out.println("JWT Filter: Error processing JWT token - " + e.getMessage());
            // Don't set authentication, let the request continue without authentication
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * Check if the endpoint should be publicly accessible
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/register") ||
               path.startsWith("/api/auth/oauth2/") ||
               path.startsWith("/api/auth/verify-2fa") ||
               path.startsWith("/api/auth/2fa/") ||
               path.startsWith("/api/auth/jwt/") ||
               path.startsWith("/api/auth/debug/") ||
               path.startsWith("/api/auth/test-password") ||
               path.startsWith("/api/products") ||  // Products can be viewed publicly
               path.startsWith("/api/backup") ||    // Backup endpoints (for now)
               path.startsWith("/actuator/") ||
               path.startsWith("/error") ||
               path.equals("/");
    }
}
