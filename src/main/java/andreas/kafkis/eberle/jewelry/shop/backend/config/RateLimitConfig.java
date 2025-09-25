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
        // 100 requests per minute
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(100).refillGreedy(100, Duration.ofMinutes(1)))
                .build();
    }

    @Bean
    public RateLimitInterceptor rateLimitInterceptor(Map<String, Bucket> buckets) {
        return new RateLimitInterceptor(buckets);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor(buckets()))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/admin/upload/**"); // Exclude upload from rate limiting
    }
}
