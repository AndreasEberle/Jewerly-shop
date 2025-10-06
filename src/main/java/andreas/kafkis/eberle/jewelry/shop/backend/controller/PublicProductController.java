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
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        try {
            List<ProductDTO> products = productService.getActiveProducts();
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

    @GetMapping("/{id}")
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
}
