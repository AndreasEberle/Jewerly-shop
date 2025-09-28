package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RateLimitingService {

    private final Map<String, RateLimitInfo> userBuckets = new ConcurrentHashMap<>();
    private final Map<String, RateLimitInfo> ipBuckets = new ConcurrentHashMap<>();
    
    // Rate limits
    private static final int USER_REQUESTS_PER_MINUTE = 60;
    private static final int IP_REQUESTS_PER_MINUTE = 100;
    private static final int AUTH_ATTEMPTS_PER_HOUR = 5;
    private static final int ORDER_CREATION_PER_HOUR = 10;
    
    // Rate limit info class
    private static class RateLimitInfo {
        private final AtomicInteger count;
        private final AtomicLong resetTime;
        private final int limit;
        private final Duration window;
        
        public RateLimitInfo(int limit, Duration window) {
            this.limit = limit;
            this.window = window;
            this.count = new AtomicInteger(0);
            this.resetTime = new AtomicLong(System.currentTimeMillis() + window.toMillis());
        }
        
        public boolean tryConsume() {
            long now = System.currentTimeMillis();
            long reset = resetTime.get();
            
            // Reset if window has passed
            if (now >= reset) {
                if (resetTime.compareAndSet(reset, now + window.toMillis())) {
                    count.set(0);
                }
            }
            
            return count.incrementAndGet() <= limit;
        }
        
        public int getRemaining() {
            return Math.max(0, limit - count.get());
        }
    }
    
    /**
     * Check if user can make a request
     */
    public boolean isUserAllowed(String userEmail) {
        RateLimitInfo bucket = userBuckets.computeIfAbsent(userEmail, 
                k -> new RateLimitInfo(USER_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)));
        boolean allowed = bucket.tryConsume();
        
        if (!allowed) {
            log.warn("Rate limit exceeded for user: {}", userEmail);
        }
        
        return allowed;
    }
    
    /**
     * Check if IP can make a request
     */
    public boolean isIpAllowed(String ipAddress) {
        RateLimitInfo bucket = ipBuckets.computeIfAbsent(ipAddress, 
                k -> new RateLimitInfo(IP_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)));
        boolean allowed = bucket.tryConsume();
        
        if (!allowed) {
            log.warn("Rate limit exceeded for IP: {}", ipAddress);
        }
        
        return allowed;
    }
    
    /**
     * Check if user can attempt authentication
     */
    public boolean canAttemptAuth(String userEmail) {
        String key = "auth_" + userEmail;
        RateLimitInfo bucket = userBuckets.computeIfAbsent(key, 
                k -> new RateLimitInfo(AUTH_ATTEMPTS_PER_HOUR, Duration.ofHours(1)));
        boolean allowed = bucket.tryConsume();
        
        if (!allowed) {
            log.warn("Authentication rate limit exceeded for user: {}", userEmail);
        }
        
        return allowed;
    }
    
    /**
     * Check if user can create an order
     */
    public boolean canCreateOrder(String userEmail) {
        String key = "order_" + userEmail;
        RateLimitInfo bucket = userBuckets.computeIfAbsent(key, 
                k -> new RateLimitInfo(ORDER_CREATION_PER_HOUR, Duration.ofHours(1)));
        boolean allowed = bucket.tryConsume();
        
        if (!allowed) {
            log.warn("Order creation rate limit exceeded for user: {}", userEmail);
        }
        
        return allowed;
    }
    
    /**
     * Get remaining tokens for user
     */
    public long getRemainingTokens(String userEmail) {
        RateLimitInfo bucket = userBuckets.get(userEmail);
        return bucket != null ? bucket.getRemaining() : USER_REQUESTS_PER_MINUTE;
    }
    
    /**
     * Get remaining tokens for IP
     */
    public long getRemainingTokensForIp(String ipAddress) {
        RateLimitInfo bucket = ipBuckets.get(ipAddress);
        return bucket != null ? bucket.getRemaining() : IP_REQUESTS_PER_MINUTE;
    }
    
    /**
     * Reset rate limits for a user (admin function)
     */
    public void resetUserRateLimit(String userEmail) {
        userBuckets.remove(userEmail);
        userBuckets.remove("auth_" + userEmail);
        userBuckets.remove("order_" + userEmail);
        log.info("Rate limits reset for user: {}", userEmail);
    }
    
    /**
     * Reset rate limits for an IP (admin function)
     */
    public void resetIpRateLimit(String ipAddress) {
        ipBuckets.remove(ipAddress);
        log.info("Rate limits reset for IP: {}", ipAddress);
    }
}

