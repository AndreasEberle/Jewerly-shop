package andreas.kafkis.eberle.jewelry.shop.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class JewelryShopBackendApplication implements WebMvcConfigurer {

    @Value("${storage.local.base-path:}")
    private String localBasePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Static resources (HTML, CSS, JS files) from classpath - more specific patterns
        registry.addResourceHandler("/*.html", "/*.css", "/*.js", "/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0); // Disable cache for development
        
        // File uploads from local filesystem
        if (StringUtils.hasText(localBasePath)) {
            String location = "file:" + localBasePath + "/";
            registry.addResourceHandler("/files/**").addResourceLocations(location);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(JewelryShopBackendApplication.class, args);
    }

}
