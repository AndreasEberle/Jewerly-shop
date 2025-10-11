package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.ProductImage;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductImageRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProductImageManagementService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductImageRepository productImageRepository;
    
    @Autowired
    private StorageService storageService;
    
    /**
     * Handle product name change by updating S3 folder structure
     * This ensures images are moved to the new folder name
     */
    @Transactional
    public void handleProductNameChange(UUID productId, String oldName, String newName) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Get all images for this product
        List<ProductImage> images = productImageRepository.findByProductId(productId);
        
        if (images.isEmpty()) {
            return; // No images to move
        }
        
        // Create new folder name (sanitized)
        String oldFolderName = sanitizeFolderName(oldName);
        String newFolderName = sanitizeFolderName(newName);
        
        if (oldFolderName.equals(newFolderName)) {
            return; // No change needed
        }
        
        // Move each image to the new folder
        for (ProductImage image : images) {
            try {
                // Get the old storage key
                String oldStorageKey = image.getStorageKey();
                if (oldStorageKey == null || !oldStorageKey.contains(oldFolderName)) {
                    continue; // Skip if not in expected folder structure
                }
                
                // Create new storage key with new folder name
                String newStorageKey = oldStorageKey.replace(oldFolderName, newFolderName);
                
                // Copy image to new location
                boolean copied = storageService.copyImage(oldStorageKey, newStorageKey);
                if (copied) {
                    // Update the image record with new storage key
                    image.setStorageKey(newStorageKey);
                    
                    // Update URL to point to new location
                    String newUrl = storageService.getFileUrl(newStorageKey);
                    image.setUrl(newUrl);
                    
                    // Update S3 URL if it exists
                    if (image.getS3Url() != null) {
                        String newS3Url = storageService.getS3FileUrl(newStorageKey);
                        image.setS3Url(newS3Url);
                    }
                    
                    // Update alt text to reflect new product name
                    String oldAltText = image.getAltText();
                    if (oldAltText != null) {
                        String newAltText;
                        if (oldAltText.contains(oldName)) {
                            // Replace old product name with new one
                            newAltText = oldAltText.replace(oldName, newName);
                        } else if (oldAltText.contains("Image")) {
                            // If it's a generic "Image X" format, update to new product name
                            newAltText = newName + " - " + oldAltText;
                        } else {
                            // If it's something else, prepend the new product name
                            newAltText = newName + " - " + oldAltText;
                        }
                        image.setAltText(newAltText);
                    } else {
                        // If no alt text, create one with the new product name
                        image.setAltText(newName + " - Product Image");
                    }
                    
                    productImageRepository.save(image);
                    
                    // Delete old image
                    storageService.deleteImage(oldStorageKey);
                    
                    log.info("Updated image for product {}: storageKey={}, url={}, altText={}", 
                        productId, newStorageKey, newUrl, image.getAltText());
                }
            } catch (Exception e) {
                // Log error but continue with other images
                System.err.println("Error moving image for product " + productId + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Clean up orphaned S3 images when a product is deleted
     */
    @Transactional
    public void cleanupProductImages(UUID productId) {
        List<ProductImage> images = productImageRepository.findByProductId(productId);
        
        for (ProductImage image : images) {
            try {
                // Delete from storage using storage key
                if (image.getStorageKey() != null) {
                    storageService.deleteImage(image.getStorageKey());
                }
                // Also delete local and S3 URLs if they exist
                if (image.getLocalUrl() != null) {
                    storageService.deleteImage(image.getLocalUrl());
                }
                if (image.getS3Url() != null) {
                    storageService.deleteImage(image.getS3Url());
                }
            } catch (Exception e) {
                System.err.println("Error deleting image " + image.getId() + ": " + e.getMessage());
            }
        }
        
        // Delete image records from database
        productImageRepository.deleteByProductId(productId);
    }
    
    /**
     * Clean up all product images (for "Delete All Products" functionality)
     */
    @Transactional
    public void cleanupAllProductImages() {
        // Get all product images
        List<ProductImage> allImages = productImageRepository.findAll();
        
        for (ProductImage image : allImages) {
            try {
                // Delete from storage using storage key
                if (image.getStorageKey() != null) {
                    storageService.deleteImage(image.getStorageKey());
                }
                // Also delete local and S3 URLs if they exist
                if (image.getLocalUrl() != null) {
                    storageService.deleteImage(image.getLocalUrl());
                }
                if (image.getS3Url() != null) {
                    storageService.deleteImage(image.getS3Url());
                }
            } catch (Exception e) {
                System.err.println("Error deleting image " + image.getId() + ": " + e.getMessage());
            }
        }
        
        // Delete all image records from database
        productImageRepository.deleteAllInBatch();
    }
    
    /**
     * Check for duplicate product names and suggest alternatives
     */
    public String suggestUniqueProductName(String desiredName) {
        String baseName = sanitizeFolderName(desiredName);
        String suggestedName = baseName;
        int counter = 1;
        
        while (productRepository.findByName(suggestedName).isPresent()) {
            suggestedName = baseName + "-" + counter;
            counter++;
        }
        
        return suggestedName;
    }
    
    /**
     * Sanitize folder name for S3 compatibility
     */
    private String sanitizeFolderName(String name) {
        if (name == null) return "";
        
        return name
            .toLowerCase()
            .replaceAll("[^a-z0-9\\-]", "-") // Replace non-alphanumeric with hyphens
            .replaceAll("-+", "-") // Replace multiple hyphens with single
            .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens
    }
    
    /**
     * Get all S3 folders for products
     */
    public List<String> getAllProductFolders() {
        return productRepository.findAll()
            .stream()
            .map(product -> sanitizeFolderName(product.getName()))
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * Delete all products and their associated images
     */
    @Transactional
    public void deleteAllProducts() {
        // Get all products
        List<Product> allProducts = productRepository.findAll();
        
        log.info("Starting deletion of {} products...", allProducts.size());
        
        // Delete each product (this will cascade to images via the cleanupProductImages method)
        for (Product product : allProducts) {
            try {
                // Clean up product images first
                cleanupProductImages(product.getId());
                
                // Delete the product
                productRepository.delete(product);
                
                log.info("Successfully deleted product: {} (ID: {})", product.getName(), product.getId());
            } catch (Exception e) {
                log.error("Error deleting product {} (ID: {}): {}", product.getName(), product.getId(), e.getMessage());
                // Continue with other products even if one fails
            }
        }
        
        log.info("Completed deletion of all products");
    }
}
