package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.ProductImage;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductImageRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.service.AnalyticsService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.ImageOptimizationService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@Tag(name = "File Upload", description = "Admin file upload operations")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    @Value("${storage.local.base-path:}")
    private String localBasePath;
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @Autowired
    private StorageService storageService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductImageRepository productImageRepository;
    
    @Autowired
    private ImageOptimizationService imageOptimizationService;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp", "gif");
    private static final List<String> PREFERRED_EXTENSIONS = Arrays.asList("webp", "jpg", "jpeg"); // WebP preferred for better compression
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    @PostMapping("/upload/{productId}")
    @Operation(summary = "Upload file for a product (legacy local storage)")
    public ResponseEntity<String> uploadFile(
            @PathVariable String productId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }
            
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body("File too large. Max size: 10MB");
            }
            
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return ResponseEntity.badRequest().body("Invalid filename");
            }
            
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                return ResponseEntity.badRequest().body("Invalid file type. Allowed: " + ALLOWED_EXTENSIONS);
            }
            
            // Create directory if it doesn't exist
            Path productDir = Paths.get(localBasePath, "products", productId);
            Files.createDirectories(productDir);
            
            // Generate unique filename
            String filename = UUID.randomUUID().toString() + "." + extension;
            Path filePath = productDir.resolve(filename);
            
            // Save file
            file.transferTo(filePath.toFile());
            
            // Track file upload
            try {
                UUID productUuid = UUID.fromString(productId);
                analyticsService.trackFileUpload(
                    productUuid, 
                    filename, 
                    file.getContentType(), 
                    file.getSize(),
                    getClientIpAddress(request), 
                    request.getHeader("User-Agent")
                );
            } catch (Exception e) {
                // Log error but don't fail the upload
                System.err.println("Failed to track file upload: " + e.getMessage());
            }
            
            // Return the storage key
            String storageKey = "products/" + productId + "/" + filename;
            return ResponseEntity.ok(storageKey);
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload file: " + e.getMessage());
        }
    }
    
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
    
    @PostMapping("/admin/upload-product-image/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Upload product image to S3 and create ProductImage record")
    public ResponseEntity<ProductImageUploadResponse> uploadProductImage(
            @PathVariable UUID productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "isPrimary", defaultValue = "false") boolean isPrimary,
            @RequestParam(value = "sortOrder", defaultValue = "1") int sortOrder,
            HttpServletRequest request) {
        
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(new ProductImageUploadResponse(false, "File is empty", null));
            }
            
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body(new ProductImageUploadResponse(false, "File too large. Max size: 10MB", null));
            }
            
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return ResponseEntity.badRequest().body(new ProductImageUploadResponse(false, "Invalid filename", null));
            }
            
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                return ResponseEntity.badRequest().body(new ProductImageUploadResponse(false, "Invalid file type. Allowed: " + ALLOWED_EXTENSIONS, null));
            }
            
            // Find product
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
            
            // Upload file to storage (S3 or local) using human-readable folder
            String storageKey = storageService.storeFileForProduct(file, product.getName(), productId.toString());
            String fileUrl = storageService.getFileUrl(storageKey);
            
            // Create ProductImage record
            ProductImage productImage = ProductImage.builder()
                    .product(product)
                    .storageKey(storageKey)
                    .url(fileUrl)
                    .altText(altText != null ? altText : product.getName() + " - Product Image")
                    .isPrimary(isPrimary)
                    .sortOrder(sortOrder)
                    .width(800) // Default width, could be extracted from image metadata
                    .height(800) // Default height, could be extracted from image metadata
                    .mimeType(file.getContentType())
                    .build();
            
            // Save to database
            ProductImage savedImage = productImageRepository.save(productImage);
            log.info("Successfully uploaded and saved product image for product {}: {}", productId, storageKey);
            
            // Track file upload
            try {
                analyticsService.trackFileUpload(
                    productId, 
                    originalFilename, 
                    file.getContentType(), 
                    file.getSize(),
                    getClientIpAddress(request), 
                    request.getHeader("User-Agent")
                );
            } catch (Exception e) {
                log.warn("Failed to track file upload: {}", e.getMessage());
            }
            
            return ResponseEntity.ok(new ProductImageUploadResponse(true, "Image uploaded successfully", savedImage));
            
        } catch (Exception e) {
            log.error("Failed to upload product image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ProductImageUploadResponse(false, "Failed to upload image: " + e.getMessage(), null));
        }
    }
    
    @PostMapping("/upload-multiple-images/{productId}")
    @Operation(summary = "Upload multiple product images at once")
    public ResponseEntity<MultipleImageUploadResponse> uploadMultipleImages(
            @PathVariable UUID productId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "altTexts", required = false) List<String> altTexts,
            @RequestParam(value = "isPrimaryIndex", defaultValue = "0") int isPrimaryIndex,
            HttpServletRequest request) {
        
        try {
            // Validate files
            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().body(new MultipleImageUploadResponse(false, "No files provided", null));
            }
            
            if (files.size() > 10) {
                return ResponseEntity.badRequest().body(new MultipleImageUploadResponse(false, "Too many files. Maximum 10 files allowed", null));
            }
            
            // Find product
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
            
            List<ProductImage> uploadedImages = new java.util.ArrayList<>();
            
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                
                // Validate each file
                if (file.isEmpty()) continue;
                
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null) continue;
                
                String extension = getFileExtension(originalFilename).toLowerCase();
                if (!ALLOWED_EXTENSIONS.contains(extension)) continue;
                
                if (file.getSize() > MAX_FILE_SIZE) continue;
                
                // Upload file using human-readable folder
                String storageKey = storageService.storeFileForProduct(file, product.getName(), productId.toString());
                String fileUrl = storageService.getFileUrl(storageKey);
                
                // Create ProductImage record
                String altText = (altTexts != null && i < altTexts.size()) ? altTexts.get(i) : product.getName() + " - Image " + (i + 1);
                boolean isPrimary = (i == isPrimaryIndex);
                
                ProductImage productImage = ProductImage.builder()
                        .product(product)
                        .storageKey(storageKey)
                        .url(fileUrl)
                        .altText(altText)
                        .isPrimary(isPrimary)
                        .sortOrder(i + 1)
                        .width(800)
                        .height(800)
                        .mimeType(file.getContentType())
                        .build();
                
                // Save to database
                ProductImage savedImage = productImageRepository.save(productImage);
                uploadedImages.add(savedImage);
                
                // Track file upload
                try {
                    analyticsService.trackFileUpload(
                        productId, 
                        originalFilename, 
                        file.getContentType(), 
                        file.getSize(),
                        getClientIpAddress(request), 
                        request.getHeader("User-Agent")
                    );
                } catch (Exception e) {
                    log.warn("Failed to track file upload for {}: {}", originalFilename, e.getMessage());
                }
            }
            
            log.info("Successfully uploaded {} images for product {}", uploadedImages.size(), productId);
            
            return ResponseEntity.ok(new MultipleImageUploadResponse(true, 
                "Successfully uploaded " + uploadedImages.size() + " images", uploadedImages));
            
        } catch (Exception e) {
            log.error("Failed to upload multiple images: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MultipleImageUploadResponse(false, "Failed to upload images: " + e.getMessage(), null));
        }
    }

    @PostMapping("/analyze-image")
    @Operation(summary = "Analyze image and provide optimization recommendations")
    public ResponseEntity<ImageAnalysisResponse> analyzeImage(
            @RequestParam("file") MultipartFile file) {
        
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ImageAnalysisResponse(false, "File is empty", null));
            }

            String mimeType = file.getContentType();
            long fileSize = file.getSize();
            String filename = file.getOriginalFilename();

            // Get optimization recommendations
            ImageOptimizationService.ImageOptimizationRecommendation recommendation = 
                imageOptimizationService.getOptimizationRecommendation(mimeType, fileSize, filename);

            ImageAnalysisResponse response = new ImageAnalysisResponse(
                true,
                "Image analysis completed",
                recommendation
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error analyzing image: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ImageAnalysisResponse(false, "Analysis failed: " + e.getMessage(), null));
        }
    }
    
    @DeleteMapping("/product-images/{imageId}")
    @Operation(summary = "Delete a product image")
    public ResponseEntity<Map<String, Object>> deleteProductImage(@PathVariable UUID imageId) {
        try {
            // Find the image
            ProductImage image = productImageRepository.findById(imageId)
                    .orElseThrow(() -> new RuntimeException("Image not found with ID: " + imageId));
            
            // Delete from S3 or local storage
            try {
                storageService.deleteFile(image.getStorageKey());
            } catch (Exception e) {
                log.warn("Failed to delete file from storage: {}", e.getMessage());
                // Continue with database deletion even if storage deletion fails
            }
            
            // Delete from database
            productImageRepository.delete(image);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("message", "Image deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to delete product image: {}", e.getMessage(), e);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete image: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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
    
    // Response DTOs
    public static class ProductImageUploadResponse {
        private boolean success;
        private String message;
        private ProductImage image;
        
        public ProductImageUploadResponse(boolean success, String message, ProductImage image) {
            this.success = success;
            this.message = message;
            this.image = image;
        }
        
        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public ProductImage getImage() { return image; }
        public void setImage(ProductImage image) { this.image = image; }
    }
    
    public static class MultipleImageUploadResponse {
        private boolean success;
        private String message;
        private List<ProductImage> images;
        
        public MultipleImageUploadResponse(boolean success, String message, List<ProductImage> images) {
            this.success = success;
            this.message = message;
            this.images = images;
        }
        
        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public List<ProductImage> getImages() { return images; }
        public void setImages(List<ProductImage> images) { this.images = images; }
    }

    public static class ImageAnalysisResponse {
        private boolean success;
        private String message;
        private ImageOptimizationService.ImageOptimizationRecommendation analysis;

        public ImageAnalysisResponse(boolean success, String message, ImageOptimizationService.ImageOptimizationRecommendation analysis) {
            this.success = success;
            this.message = message;
            this.analysis = analysis;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public ImageOptimizationService.ImageOptimizationRecommendation getAnalysis() { return analysis; }
        public void setAnalysis(ImageOptimizationService.ImageOptimizationRecommendation analysis) { this.analysis = analysis; }
    }
}
