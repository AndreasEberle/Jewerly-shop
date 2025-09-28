package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    // Cache TTL constants
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);
    private static final Duration USER_TTL = Duration.ofHours(1);
    private static final Duration PRODUCT_TTL = Duration.ofMinutes(15);
    private static final Duration ORDER_TTL = Duration.ofMinutes(5);
    
    /**
     * Store value in cache with default TTL
     */
    public void put(String key, Object value) {
        put(key, value, DEFAULT_TTL);
    }
    
    /**
     * Store value in cache with custom TTL
     */
    public void put(String key, Object value, Duration ttl) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jsonValue, ttl.toMillis(), TimeUnit.MILLISECONDS);
            log.debug("Cached value for key: {} with TTL: {}", key, ttl);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize value for caching: {}", e.getMessage());
        }
    }
    
    /**
     * Get value from cache
     */
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            String jsonValue = redisTemplate.opsForValue().get(key);
            if (jsonValue != null) {
                T value = objectMapper.readValue(jsonValue, type);
                log.debug("Cache hit for key: {}", key);
                return Optional.of(value);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize cached value: {}", e.getMessage());
        }
        log.debug("Cache miss for key: {}", key);
        return Optional.empty();
    }
    
    /**
     * Delete value from cache
     */
    public void delete(String key) {
        redisTemplate.delete(key);
        log.debug("Deleted cache entry for key: {}", key);
    }
    
    /**
     * Delete multiple keys matching pattern
     */
    public void deletePattern(String pattern) {
        redisTemplate.delete(redisTemplate.keys(pattern));
        log.debug("Deleted cache entries matching pattern: {}", pattern);
    }
    
    /**
     * Check if key exists in cache
     */
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    /**
     * Get TTL for a key
     */
    public Duration getTtl(String key) {
        Long ttlSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttlSeconds != null && ttlSeconds > 0 ? Duration.ofSeconds(ttlSeconds) : Duration.ZERO;
    }
    
    // Specific cache methods for different entities
    
    /**
     * Cache user data
     */
    public void cacheUser(String userId, Object user) {
        put("user:" + userId, user, USER_TTL);
    }
    
    /**
     * Get cached user data
     */
    public <T> Optional<T> getCachedUser(String userId, Class<T> type) {
        return get("user:" + userId, type);
    }
    
    /**
     * Cache product data
     */
    public void cacheProduct(String productId, Object product) {
        put("product:" + productId, product, PRODUCT_TTL);
    }
    
    /**
     * Get cached product data
     */
    public <T> Optional<T> getCachedProduct(String productId, Class<T> type) {
        return get("product:" + productId, type);
    }
    
    /**
     * Cache order data
     */
    public void cacheOrder(String orderId, Object order) {
        put("order:" + orderId, order, ORDER_TTL);
    }
    
    /**
     * Get cached order data
     */
    public <T> Optional<T> getCachedOrder(String orderId, Class<T> type) {
        return get("order:" + orderId, type);
    }
    
    /**
     * Cache JWT token blacklist
     */
    public void blacklistToken(String token, Duration ttl) {
        put("blacklist:" + token, "true", ttl);
    }
    
    /**
     * Check if token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        return exists("blacklist:" + token);
    }
    
    /**
     * Cache rate limit data
     */
    public void cacheRateLimit(String key, int remaining, Duration ttl) {
        put("rate_limit:" + key, remaining, ttl);
    }
    
    /**
     * Get cached rate limit data
     */
    public Optional<Integer> getCachedRateLimit(String key) {
        return get("rate_limit:" + key, Integer.class);
    }
    
    /**
     * Clear all cache
     */
    public void clearAll() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        log.info("Cleared all cache");
    }
    
    /**
     * Get cache statistics
     */
    public CacheStats getStats() {
        return CacheStats.builder()
                .totalKeys(redisTemplate.getConnectionFactory().getConnection().dbSize())
                .build();
    }
    
    @lombok.Data
    @lombok.Builder
    public static class CacheStats {
        private long totalKeys;
    }
}

