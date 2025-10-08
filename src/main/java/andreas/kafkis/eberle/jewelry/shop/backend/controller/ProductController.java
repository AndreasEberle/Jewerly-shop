package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.CreateProductRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.ProductDTO;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.UpdateProductRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    public ResponseEntity<ProductDTO> createProduct(@RequestBody CreateProductRequest request) {
        try {
            log.info("Creating product: {}", request.getName());
            
            // Check if product name is unique
            if (!productService.isProductNameUnique(request.getName())) {
                return ResponseEntity.badRequest().build();
            }

            ProductDTO product = productService.createProduct(request);
            
            log.info("Product created successfully: {} (ID: {})", product.getName(), product.getId());
            
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all products")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        try {
            List<ProductDTO> products = productService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Error getting products: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable UUID id) {
        try {
            ProductDTO product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            log.error("Error getting product: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable UUID id, @RequestBody UpdateProductRequest request) {
        try {
            // Check if new name is unique (excluding current product)
            if (request.getName() != null) {
                if (!productService.isProductNameUnique(request.getName(), id)) {
                    return ResponseEntity.badRequest().build();
                }
            }

            ProductDTO product = productService.updateProduct(id, request);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            log.error("Error updating product: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete product")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting product: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/generate-sku")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate SKU from product name")
    public ResponseEntity<String> generateSku(@RequestParam String name) {
        try {
            String sku = productService.generateSku(name);
            return ResponseEntity.ok(sku);
        } catch (Exception e) {
            log.error("Error generating SKU: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}