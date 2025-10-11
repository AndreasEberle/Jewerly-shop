package andreas.kafkis.eberle.jewelry.shop.backend.config;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import andreas.kafkis.eberle.jewelry.shop.backend.interceptor.RateLimitInterceptor;
import io.github.bucket4j.Bucket;

@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    @Bean
    public Map<String, Bucket> buckets() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public Bucket createNewBucket() {
        // 1000 requests per minute - Much more generous
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(1000).refillGreedy(1000, Duration.ofMinutes(1)))
                .build();
    }

    @Bean
    public RateLimitInterceptor rateLimitInterceptor(Map<String, Bucket> buckets) {
        return new RateLimitInterceptor(buckets);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor(buckets()))
                .addPathPatterns("/api/auth/login", "/api/auth/register", "/api/orders/create", "/api/payments/process") // Only critical endpoints
                .excludePathPatterns(
                    "/api/admin/**",  // Exclude ALL admin endpoints
                    "/api/public/**",  // Exclude public endpoints
                    "/api/products/**",  // Exclude product browsing
                    "/api/categories/**",  // Exclude category browsing
                    "/api/favorites/**",  // Exclude favorites
                    "/api/reviews/**",  // Exclude reviews
                    "/api/section-styles/**",  // Exclude section styles
                    "/api/hero-slider/**",  // Exclude hero slider
                    "/api/branding/**",  // Exclude branding
                    "/api/special-offer-descriptions/**",  // Exclude special offers
                    "/api/upload/**",  // Exclude uploads
                    "/api/storage/**",  // Exclude storage
                    "/api/background-images/**",  // Exclude background images
                    "/api/system-config/**",  // Exclude system config
                    "/api/health/**",  // Exclude health checks
                    "/api/currency/**",  // Exclude currency
                    "/api/language/**"  // Exclude language
                );
    }
}
