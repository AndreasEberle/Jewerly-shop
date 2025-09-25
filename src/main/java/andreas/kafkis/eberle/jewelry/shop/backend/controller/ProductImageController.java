package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.ProductImage;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductImageRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/products")
public class ProductImageController {
    
    private final ProductImageRepository productImageRepository;
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @Value("${storage.public-base-url:}")
    private String publicBaseUrl;
    
    public ProductImageController(ProductImageRepository productImageRepository) {
        this.productImageRepository = productImageRepository;
    }
    
    @GetMapping("/{productId}/images")
    public ResponseEntity<List<ProductImage>> getProductImages(@PathVariable UUID productId, HttpServletRequest request) {
        // Track product view
        analyticsService.trackProductView(productId, getClientIpAddress(request), request.getHeader("User-Agent"));
        
        List<ProductImage> images = productImageRepository.findByProductIdOrderByIsPrimaryDescSortOrderAsc(productId);
        
        // Build public URLs for each image
        images.forEach(image -> {
            if (image.getUrl() == null && image.getStorageKey() != null) {
                String publicUrl = publicBaseUrl + image.getStorageKey();
                image.setUrl(publicUrl);
            }
        });
        
        return ResponseEntity.ok(images);
    }
    
    @GetMapping("/{productId}/images/primary")
    public ResponseEntity<ProductImage> getPrimaryImage(@PathVariable UUID productId, HttpServletRequest request) {
        // Track product view
        analyticsService.trackProductView(productId, getClientIpAddress(request), request.getHeader("User-Agent"));
        
        List<ProductImage> images = productImageRepository.findByProductIdOrderByIsPrimaryDescSortOrderAsc(productId);
        
        if (images.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        ProductImage primaryImage = images.get(0);
        
        // Build public URL if not set
        if (primaryImage.getUrl() == null && primaryImage.getStorageKey() != null) {
            String publicUrl = publicBaseUrl + primaryImage.getStorageKey();
            primaryImage.setUrl(publicUrl);
        }
        
        return ResponseEntity.ok(primaryImage);
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
    
}
