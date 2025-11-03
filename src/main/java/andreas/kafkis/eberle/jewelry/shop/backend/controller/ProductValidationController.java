package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/products")
@Tag(name = "Product Validation", description = "Product validation operations")
@RequiredArgsConstructor
@Slf4j
public class ProductValidationController {

    @Autowired
    private ProductService productService;

    @GetMapping("/validate-name")
    @Operation(summary = "Check if product name is unique")
    public ResponseEntity<ProductNameValidationResponse> validateProductName(
            @RequestParam String name,
            @RequestParam(required = false) UUID excludeProductId) {
        
        try {
            boolean isUnique = productService.isProductNameUnique(name, excludeProductId);
            
            ProductNameValidationResponse response = new ProductNameValidationResponse(
                isUnique,
                isUnique ? "Product name is available" : "Product name already exists",
                name
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error validating product name: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ProductNameValidationResponse(false, "Validation failed", name));
        }
    }

    // Response DTO
    public static class ProductNameValidationResponse {
        private boolean isUnique;
        private String message;
        private String name;

        public ProductNameValidationResponse(boolean isUnique, String message, String name) {
            this.isUnique = isUnique;
            this.message = message;
            this.name = name;
        }

        // Getters and setters
        public boolean isUnique() { return isUnique; }
        public void setUnique(boolean unique) { isUnique = unique; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}





