package andreas.kafkis.eberle.jewelry.shop.backend.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.ErrorResponse;
import andreas.kafkis.eberle.jewelry.shop.backend.service.RateLimitingService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter implements Filter {

    private final RateLimitingService rateLimitingService;
    private final ObjectMapper objectMapper;
    
    // Endpoints that should be rate limited - Only the most critical ones
    private static final List<String> RATE_LIMITED_PATHS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/orders/create",  // Only rate limit order creation, not browsing
            "/api/payments/process"  // Only rate limit payment processing, not browsing
    );
    
    // Endpoints that should be excluded from rate limiting
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/api/health",
            "/swagger-ui",
            "/api-docs",
            "/oauth2",
            "/api/products",
            "/api/currency",
            "/api/language",
            "/api/public",
            "/api/user/favorites",
            "/api/admin",  // Exclude ALL admin endpoints
            "/api/categories",  // Exclude category browsing
            "/api/reviews",  // Exclude reviews
            "/api/section-styles",  // Exclude section styles
            "/api/hero-slider",  // Exclude hero slider
            "/api/branding",  // Exclude branding
            "/api/special-offer-descriptions",  // Exclude special offers
            "/api/upload",  // Exclude uploads
            "/api/storage",  // Exclude storage
            "/api/background-images",  // Exclude background images
            "/api/system-config"  // Exclude system config
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestPath = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        
        // Skip rate limiting for excluded paths
        if (isExcludedPath(requestPath)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Only rate limit specific paths
        if (!shouldRateLimit(requestPath, method)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Get client IP
        String clientIp = getClientIpAddress(httpRequest);
        
        // Check IP-based rate limiting first
        if (!rateLimitingService.isIpAllowed(clientIp)) {
            log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, requestPath);
            sendRateLimitResponse(httpResponse, "IP rate limit exceeded", 
                    rateLimitingService.getRemainingTokensForIp(clientIp));
            return;
        }
        
        // Check user-based rate limiting if authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getName())) {
            
            String userEmail = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            
            // Skip rate limiting for admin users on most endpoints
            if (isAdmin && !requestPath.startsWith("/api/auth/") && !requestPath.startsWith("/api/orders/create") && !requestPath.startsWith("/api/payments/process")) {
                chain.doFilter(request, response);
                return;
            }
            
            // Special rate limiting for auth endpoints
            if (requestPath.startsWith("/api/auth/")) {
                boolean canAuth = isAdmin ? 
                    rateLimitingService.canAdminAttemptAuth(userEmail) : 
                    rateLimitingService.canAttemptAuth(userEmail);
                    
                if (!canAuth) {
                    log.warn("Auth rate limit exceeded for user: {} on path: {}", userEmail, requestPath);
                    sendRateLimitResponse(httpResponse, "Authentication rate limit exceeded", 
                            rateLimitingService.getRemainingTokens(userEmail));
                    return;
                }
            }
            
            // Special rate limiting for order creation
            if (requestPath.startsWith("/api/orders") && "POST".equals(method)) {
                boolean canCreateOrder = isAdmin ? 
                    rateLimitingService.canAdminCreateOrder(userEmail) : 
                    rateLimitingService.canCreateOrder(userEmail);
                    
                if (!canCreateOrder) {
                    log.warn("Order creation rate limit exceeded for user: {} on path: {}", userEmail, requestPath);
                    sendRateLimitResponse(httpResponse, "Order creation rate limit exceeded", 
                            rateLimitingService.getRemainingTokens(userEmail));
                    return;
                }
            }
            
            // General user rate limiting
            boolean isAllowed = isAdmin ? 
                rateLimitingService.isAdminAllowed(userEmail) : 
                rateLimitingService.isUserAllowed(userEmail);
                
            if (!isAllowed) {
                log.warn("User rate limit exceeded for user: {} on path: {}", userEmail, requestPath);
                sendRateLimitResponse(httpResponse, "User rate limit exceeded", 
                        rateLimitingService.getRemainingTokens(userEmail));
                return;
            }
        }
        
        // Continue with the request
        chain.doFilter(request, response);
    }
    
    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }
    
    private boolean shouldRateLimit(String path, String method) {
        // Only rate limit specific paths
        return RATE_LIMITED_PATHS.stream().anyMatch(path::startsWith);
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private void sendRateLimitResponse(HttpServletResponse response, String message, long remainingTokens) 
            throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remainingTokens));
        response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60000)); // 1 minute
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .error("Too Many Requests")
                .message(message)
                .timestamp(java.time.LocalDateTime.now())
                .details(java.util.Map.of(
                        "remainingTokens", remainingTokens,
                        "retryAfter", 60
                ))
                .build();
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
