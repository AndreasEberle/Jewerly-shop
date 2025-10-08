package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.ProductImage;
import andreas.kafkis.eberle.jewelry.shop.backend.service.ProductImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/product-images")
@Tag(name = "Product Image Management", description = "Admin operations for managing product images")
@RequiredArgsConstructor
@Slf4j
public class ProductImageManagementController {

    @Autowired
    private ProductImageService productImageService;

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get all images for a product")
    public ResponseEntity<List<ProductImage>> getProductImages(@PathVariable UUID productId) {
        try {
            List<ProductImage> images = productImageService.getProductImages(productId);
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            log.error("Error getting product images for product {}: {}", productId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/product/{productId}/primary")
    @Operation(summary = "Get primary image for a product")
    public ResponseEntity<ProductImage> getPrimaryImage(@PathVariable UUID productId) {
        try {
            ProductImage image = productImageService.getPrimaryImage(productId);
            if (image != null) {
                return ResponseEntity.ok(image);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting primary image for product {}: {}", productId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/add-amazon-images")
    @Operation(summary = "Add Amazon-style images to a product")
    public ResponseEntity<String> addAmazonImages(@RequestBody AddAmazonImagesRequest request) {
        try {
            productImageService.addAmazonImages(request.getProductSku(), request.getImageUrls(), request.isPrimary());
            return ResponseEntity.ok("Successfully added " + request.getImageUrls().size() + " Amazon images to product " + request.getProductSku());
        } catch (Exception e) {
            log.error("Error adding Amazon images: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error adding Amazon images: " + e.getMessage());
        }
    }

    @PostMapping("/update-amazon-images")
    @Operation(summary = "Update product images with Amazon URLs")
    public ResponseEntity<String> updateWithAmazonImages(@RequestBody AddAmazonImagesRequest request) {
        try {
            productImageService.updateWithAmazonImages(request.getProductSku(), request.getImageUrls());
            return ResponseEntity.ok("Successfully updated product " + request.getProductSku() + " with " + request.getImageUrls().size() + " Amazon images");
        } catch (Exception e) {
            log.error("Error updating with Amazon images: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error updating with Amazon images: " + e.getMessage());
        }
    }

    // Request DTO
    public static class AddAmazonImagesRequest {
        private String productSku;
        private List<String> imageUrls;
        private boolean isPrimary = true;

        // Getters and setters
        public String getProductSku() { return productSku; }
        public void setProductSku(String productSku) { this.productSku = productSku; }
        
        public List<String> getImageUrls() { return imageUrls; }
        public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
        
        public boolean isPrimary() { return isPrimary; }
        public void setPrimary(boolean primary) { isPrimary = primary; }
    }
}