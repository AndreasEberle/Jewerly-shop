package andreas.kafkis.eberle.jewelry.shop.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:8080", "http://127.0.0.1:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(true)  // Allow credentials for HTTP-only cookies
                .maxAge(3600);
        
        // Firefox-specific mapping for analytics
        registry.addMapping("/analytics-dashboard.html")
                .allowedOrigins("*")
                .allowedMethods("GET")
                .allowedHeaders("*");
    }
}
