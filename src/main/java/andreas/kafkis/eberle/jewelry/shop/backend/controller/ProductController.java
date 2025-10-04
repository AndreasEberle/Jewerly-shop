package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.service.ProductService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Get all products with pagination and search
     */
    @GetMapping
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String gemstone,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "true") Boolean active
    ) {
        // Create sort direction
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        
        // Create pageable object
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        // Search products
        Page<Product> products = productService.searchProducts(
                search, category, material, gemstone, minPrice, maxPrice, active, pageable
        );
        
        return ResponseEntity.ok(products);
    }

    /**
     * Get all products (simple list for backward compatibility)
     */
    @GetMapping("/all")
    public ResponseEntity<List<Product>> listAll() {
        return ResponseEntity.ok(productService.listAll());
    }

    /**
     * Get active products only
     */
    @GetMapping("/active")
    public ResponseEntity<List<Product>> getActiveProducts() {
        return ResponseEntity.ok(productService.findActiveProducts());
    }

    /**
     * Get featured products
     */
    @GetMapping("/featured")
    public ResponseEntity<List<Product>> getFeaturedProducts(
            @RequestParam(defaultValue = "8") int limit,
            @RequestHeader(value = "X-Currency", defaultValue = "CHF") String currency) {
        return ResponseEntity.ok(productService.findFeaturedProducts(limit, currency));
    }

    /**
     * Get products by category
     */
    @GetMapping("/category/{categoryName}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String categoryName) {
        return ResponseEntity.ok(productService.findByCategory(categoryName));
    }

    /**
     * Get single product by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> get(@PathVariable UUID id) {
        Product product = productService.get(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Create new product (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> create(@Valid @RequestBody Product product) {
        Product createdProduct = productService.create(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }
}


