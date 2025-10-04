package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/products")
@Tag(name = "Product Management", description = "Admin operations for products")
@Slf4j
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody CreateProductRequest request) {
        try {
            log.info("Creating product: {}", request.getName());
            
            // Validate required fields
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().build();
            }

            // Check if product name is unique
            if (!productService.isProductNameUnique(request.getName())) {
                return ResponseEntity.badRequest().build();
            }

            // Create product
            Product product = Product.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .priceCents(request.getPrice().multiply(BigDecimal.valueOf(100)).longValue())
                    .sku(request.getSku())
                    .active(request.getActive() != null ? request.getActive() : true)
                    .build();

            Product savedProduct = productService.save(product);
            
            log.info("Product created successfully: {} (ID: {})", savedProduct.getName(), savedProduct.getId());
            
            ProductResponse response = ProductResponse.builder()
                    .id(savedProduct.getId())
                    .name(savedProduct.getName())
                    .description(savedProduct.getDescription())
                    .price(savedProduct.getPriceCents() / 100.0)
                    .sku(savedProduct.getSku())
                    .active(savedProduct.isActive())
                    .createdAt(savedProduct.getCreatedAt())
                    .updatedAt(savedProduct.getUpdatedAt())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        try {
            List<Product> products = productService.findAll();
            List<ProductResponse> responses = products.stream()
                    .map(this::buildProductResponse)
                    .toList();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting products: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable UUID id) {
        try {
            Product product = productService.findById(id);
            if (product == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(buildProductResponse(product));
        } catch (Exception e) {
            log.error("Error getting product: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable UUID id, @RequestBody UpdateProductRequest request) {
        try {
            Product product = productService.findById(id);
            if (product == null) {
                return ResponseEntity.notFound().build();
            }

            // Check if new name is unique (excluding current product)
            if (request.getName() != null && !request.getName().equals(product.getName())) {
                if (!productService.isProductNameUnique(request.getName(), id)) {
                    return ResponseEntity.badRequest().build();
                }
            }

            // Update fields
            if (request.getName() != null) {
                product.setName(request.getName());
            }
            if (request.getDescription() != null) {
                product.setDescription(request.getDescription());
            }
            if (request.getPrice() != null) {
                product.setPriceCents(request.getPrice().multiply(BigDecimal.valueOf(100)).longValue());
            }
            if (request.getSku() != null) {
                product.setSku(request.getSku());
            }
            if (request.getActive() != null) {
                product.setActive(request.getActive());
            }

            Product savedProduct = productService.save(product);
            return ResponseEntity.ok(buildProductResponse(savedProduct));
        } catch (Exception e) {
            log.error("Error updating product: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private ProductResponse buildProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPriceCents() / 100.0)
                .sku(product.getSku())
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    @Data
    public static class CreateProductRequest {
        private String name;
        private String description;
        private BigDecimal price;
        private String sku;
        private Boolean active;
        private String category;
        private List<String> tags;
        private Boolean specialOffer;
        private BigDecimal specialOfferPrice;
        private String specialOfferDescription;
    }

    @Data
    public static class UpdateProductRequest {
        private String name;
        private String description;
        private BigDecimal price;
        private String sku;
        private Boolean active;
    }

    @Data
    @lombok.Builder
    public static class ProductResponse {
        private UUID id;
        private String name;
        private String description;
        private Double price;
        private String sku;
        private Boolean active;
        private java.time.OffsetDateTime createdAt;
        private java.time.OffsetDateTime updatedAt;
    }
}