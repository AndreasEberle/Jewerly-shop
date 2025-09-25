package andreas.kafkis.eberle.jewelry.shop.backend.interceptor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, Bucket> buckets;

    public RateLimitInterceptor(Map<String, Bucket> buckets) {
        this.buckets = buckets;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientId = getClientId(request);
        Bucket bucket = buckets.computeIfAbsent(clientId, k -> createNewBucket());
        
        if (bucket.tryConsume(1)) {
            return true;
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(bucket.getAvailableTokens()));
            return false;
        }
    }
    
    private String getClientId(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(100).refillGreedy(100, java.time.Duration.ofMinutes(1)))
                .build();
    }
}
