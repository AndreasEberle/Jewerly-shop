package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.ProductDTO;
import andreas.kafkis.eberle.jewelry.shop.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Public Products", description = "Public product viewing endpoints")
@Slf4j
public class PublicProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    @Operation(summary = "Get all active products")
    public ResponseEntity<List<ProductDTO>> getAllProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String gemstone,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice) {
        try {
            List<ProductDTO> products;
            if (search != null && !search.trim().isEmpty()) {
                // Use searchProducts for filtering and convert to DTOs
                products = productService.searchProducts(
                    search, category, material, gemstone, minPrice, maxPrice, true, 
                    org.springframework.data.domain.Pageable.unpaged()
                ).getContent().stream()
                    .map(productService::convertToDTO)
                    .collect(java.util.stream.Collectors.toList());
            } else {
                // Get all active products
                products = productService.getActiveProducts();
            }
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error getting products: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured products")
    public ResponseEntity<List<ProductDTO>> getFeaturedProducts(
            @RequestParam(defaultValue = "8") int limit) {
        try {
            List<ProductDTO> products = productService.getFeaturedProducts(limit);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error getting featured products: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable UUID id) {
        try {
            ProductDTO product = productService.getProductById(id);
            if (!product.isActive()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            log.error("Error getting product: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU")
    public ResponseEntity<ProductDTO> getProductBySku(@PathVariable String sku) {
        try {
            ProductDTO product = productService.getProductBySku(sku);
            if (!product.isActive()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            log.error("Error getting product by SKU: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
    
    // This catch-all route should be last and tries slug first, then ID
    // Note: Must come after all other specific routes like /featured, /sku/{sku}, /id/{id}
    @GetMapping("/{identifier}")
    @Operation(summary = "Get product by slug (fallback for user-friendly URLs)")
    public ResponseEntity<ProductDTO> getProductByIdentifier(@PathVariable String identifier) {
        try {
            // Skip if it matches reserved paths
            if (identifier.equals("featured") || identifier.equals("sku") || identifier.equals("id")) {
                return ResponseEntity.notFound().build();
            }
            
            // First try as slug (most common case for user-friendly URLs)
            try {
                ProductDTO product = productService.getProductBySlug(identifier);
                if (product.isActive()) {
                    return ResponseEntity.ok(product);
                }
            } catch (Exception e) {
                // Not a slug, try as UUID
            }
            
            // Try as UUID
            try {
                UUID id = UUID.fromString(identifier);
                ProductDTO product = productService.getProductById(id);
                if (product.isActive()) {
                    return ResponseEntity.ok(product);
                }
            } catch (IllegalArgumentException e) {
                // Not a valid UUID either
            }
            
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting product: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
}




