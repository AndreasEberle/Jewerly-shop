package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.ProductImage;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductImageRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.service.StorageService;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class AdminProductImageController {
    
    @Autowired
    private ProductImageRepository productImageRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private StorageService storageService;
    
    /**
     * Get all images for a product
     */
    @GetMapping("/{productId}/images")
    public ResponseEntity<List<ProductImage>> getProductImages(@PathVariable UUID productId) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderByIsPrimaryDescSortOrderAsc(productId);
        return ResponseEntity.ok(images);
    }
    
    /**
     * Delete a specific image
     */
    @DeleteMapping("/{productId}/images/{imageId}")
    public ResponseEntity<String> deleteImage(@PathVariable UUID productId, @PathVariable UUID imageId) {
        try {
            ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));
            
            // Delete from storage if storage key exists
            if (image.getStorageKey() != null && !image.getStorageKey().isEmpty()) {
                boolean deleted = storageService.deleteFile(image.getStorageKey());
                if (!deleted) {
                    System.err.println("Failed to delete image from storage: " + image.getStorageKey());
                }
            }
            
            // Delete from database
            productImageRepository.delete(image);
            
            return ResponseEntity.ok("Image deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to delete image: " + e.getMessage());
        }
    }
    
    /**
     * Set an image as featured (primary)
     */
    @PutMapping("/{productId}/images/{imageId}/featured")
    public ResponseEntity<String> setFeaturedImage(@PathVariable UUID productId, @PathVariable UUID imageId) {
        try {
            // Get all images for this product
            List<ProductImage> allImages = productImageRepository.findByProductIdOrderByIsPrimaryDescSortOrderAsc(productId);
            
            // Find the image to set as featured
            ProductImage imageToFeature = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));
            
            // Unset all other images as primary and reassign sort orders
            int sortOrder = 2; // Start from 2, featured will be 1
            for (ProductImage img : allImages) {
                if (img.getId().equals(imageId)) {
                    // This is the image we want to feature
                    img.setPrimary(true);
                    img.setSortOrder(1); // Featured image always has sort order 1
                } else {
                    // This is not the featured image
                    img.setPrimary(false);
                    img.setSortOrder(sortOrder++);
                }
                productImageRepository.save(img);
            }
            
            return ResponseEntity.ok("Featured image updated successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to set featured image: " + e.getMessage());
        }
    }
    
    /**
     * Reorder images with new positions
     */
    @PutMapping("/{productId}/images/reorder")
    public ResponseEntity<String> reorderImages(@PathVariable UUID productId, @RequestBody ReorderImagesRequest request) {
        try {
            System.out.println("Reorder request: productId=" + productId + ", imageIds=" + request.getImageIds());
            List<ProductImage> images = productImageRepository.findByProductIdOrderByIsPrimaryDescSortOrderAsc(productId);
            
            // Validate that all image IDs exist
            for (String imageId : request.getImageIds()) {
                boolean found = images.stream().anyMatch(img -> img.getId().toString().equals(imageId));
                if (!found) {
                    return ResponseEntity.badRequest().body("Image not found: " + imageId);
                }
            }
            
            // Ensure featured image stays at position 0
            ProductImage featuredImage = images.stream()
                .filter(ProductImage::isPrimary)
                .findFirst()
                .orElse(null);
            
            if (featuredImage != null && !request.getImageIds().get(0).equals(featuredImage.getId().toString())) {
                return ResponseEntity.badRequest().body("Featured image must stay at the top");
            }
            
            // Reassign sort orders based on new positions
            for (int i = 0; i < request.getImageIds().size(); i++) {
                String imageId = request.getImageIds().get(i);
                ProductImage image = images.stream()
                    .filter(img -> img.getId().toString().equals(imageId))
                    .findFirst()
                    .orElse(null);
                
                if (image != null) {
                    image.setSortOrder(i + 1); // 1, 2, 3, 4...
                    productImageRepository.save(image);
                    System.out.println("Updated image " + imageId + " to sort order " + (i + 1));
                }
            }
            
            System.out.println("Reorder completed successfully");
            return ResponseEntity.ok("Image order updated successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to reorder images: " + e.getMessage());
        }
    }
    
    /**
     * Move an image up or down in the order (legacy support)
     */
    @PutMapping("/{productId}/images/{imageId}/move")
    public ResponseEntity<String> moveImage(@PathVariable UUID productId, @PathVariable UUID imageId, @RequestBody MoveImageRequest request) {
        try {
            System.out.println("Move request: productId=" + productId + ", imageId=" + imageId + ", direction=" + request.getDirection());
            List<ProductImage> images = productImageRepository.findByProductIdOrderByIsPrimaryDescSortOrderAsc(productId);
            
            // Find the current image
            int currentIndex = -1;
            for (int i = 0; i < images.size(); i++) {
                if (images.get(i).getId().equals(imageId)) {
                    currentIndex = i;
                    break;
                }
            }
            
            if (currentIndex == -1) {
                return ResponseEntity.badRequest().body("Image not found");
            }
            
            // Calculate new index
            int newIndex;
            if ("up".equals(request.getDirection())) {
                newIndex = Math.max(0, currentIndex - 1);
            } else if ("down".equals(request.getDirection())) {
                newIndex = Math.min(images.size() - 1, currentIndex + 1);
            } else {
                return ResponseEntity.badRequest().body("Invalid direction. Use 'up' or 'down'");
            }
            
            if (currentIndex == newIndex) {
                return ResponseEntity.ok("Image is already in the correct position");
            }
            
            // Prevent moving featured image from position 0
            ProductImage currentImage = images.get(currentIndex);
            if (currentImage.isPrimary() && currentIndex == 0) {
                return ResponseEntity.badRequest().body("Featured image must stay at the top");
            }
            
            // Create new order array by moving the item
            List<String> newOrder = new ArrayList<>();
            
            // Add all images in their current order, but skip the one being moved
            for (int i = 0; i < images.size(); i++) {
                if (i != currentIndex) {
                    newOrder.add(images.get(i).getId().toString());
                }
            }
            
            // Insert the moved item at the new position
            newOrder.add(newIndex, imageId.toString());
            
            // Use the reorder endpoint
            ReorderImagesRequest reorderRequest = new ReorderImagesRequest();
            reorderRequest.setImageIds(newOrder);
            return reorderImages(productId, reorderRequest);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to move image: " + e.getMessage());
        }
    }
    
    /**
     * Update image alt text
     */
    @PutMapping("/{productId}/images/{imageId}/alt-text")
    public ResponseEntity<String> updateAltText(@PathVariable UUID productId, @PathVariable UUID imageId, @RequestBody AltTextRequest request) {
        try {
            ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));
            
            image.setAltText(request.getAltText());
            productImageRepository.save(image);
            
            return ResponseEntity.ok("Alt text updated successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to update alt text: " + e.getMessage());
        }
    }
    
    // Request DTOs
    public static class MoveImageRequest {
        private String direction;
        
        public String getDirection() {
            return direction;
        }
        
        public void setDirection(String direction) {
            this.direction = direction;
        }
    }
    
    public static class AltTextRequest {
        private String altText;
        
        public String getAltText() {
            return altText;
        }
        
        public void setAltText(String altText) {
            this.altText = altText;
        }
    }
    
    public static class ReorderImagesRequest {
        private List<String> imageIds;
        
        public List<String> getImageIds() {
            return imageIds;
        }
        
        public void setImageIds(List<String> imageIds) {
            this.imageIds = imageIds;
        }
    }
}
