package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.ProductImageStorageResult;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.BackgroundImage;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.BackgroundImageRepository;

@Service
public class BackgroundImageService {
    
    private static final Logger log = LoggerFactory.getLogger(BackgroundImageService.class);
    
    @Autowired
    private BackgroundImageRepository backgroundImageRepository;
    
    @Autowired
    private StorageService storageService;
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    // Define allowed sections for the main landing page
    public static final String[] LANDING_PAGE_SECTIONS = {
        "hero",           // Main hero banner
        "navigation",     // Navigation bar background
        "featured_products", // Featured products section
        "testimonials",   // Testimonials section
        "footer",         // Footer background
        "about",          // About section
        "contact",        // Contact section
        "story_craftsmanship", // Story section: Craftsmanship
        "story_materials",     // Story section: Materials & Inspiration
        "story_personal",      // Story section: Personal Connection
        "story_explore",       // Story section: Explore Collection
        "story_craftsmanship_bg", // Story section: Craftsmanship background
        "story_materials_bg",     // Story section: Materials background
        "story_personal_bg",      // Story section: Personal Connection background
        "story_explore_bg",       // Story section: Explore Collection background
        "washi_texture"        // Washi paper texture for hero background
    };
    
    // Define allowed MIME types
    public static final String[] ALLOWED_MIME_TYPES = {
        "image/jpeg",
        "image/jpg", 
        "image/png",
        "image/webp",
        "image/gif",       // Support for animated GIFs
        "video/mp4",       // Support for MP4 videos
        "video/webm",      // Support for WebM videos
        "video/quicktime" // Support for MOV videos
    };
    
    // Get active background image for a section
    public Optional<BackgroundImage> getActiveBackgroundImage(String sectionName) {
        return backgroundImageRepository.findBySectionNameAndIsActiveTrue(sectionName);
    }
    
    // Get all background images for a section
    public List<BackgroundImage> getBackgroundImagesForSection(String sectionName) {
        return backgroundImageRepository.findBySectionNameOrderByCreatedAtDesc(sectionName);
    }
    
    // Get all active background images
    public List<BackgroundImage> getAllActiveBackgroundImages() {
        return backgroundImageRepository.findByIsActiveTrueOrderBySectionName();
    }
    
    // Get all background images grouped by section
    public List<BackgroundImage> getAllBackgroundImages() {
        return backgroundImageRepository.findAllOrderBySectionNameAndCreatedAt();
    }
    
    // Upload and save a new background image
    public BackgroundImage uploadBackgroundImage(String sectionName, MultipartFile file) throws IOException {
        // Validate section name
        if (!isValidSection(sectionName)) {
            throw new IllegalArgumentException("Invalid section name: " + sectionName);
        }
        
        // Validate file type
        if (!isValidMimeType(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file type: " + file.getContentType());
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String imageName = sectionName + "_" + System.currentTimeMillis() + fileExtension;
        
        // Get storage type from system config
        String storageType = getStorageType();
        
        // Store file using existing storage service
        String localUrl = null;
        String s3Url = null;
        
        try {
            // Use the dual storage method to store in both local and S3 based on storage type
            String folder = "backgrounds/" + sectionName;
            ProductImageStorageResult storageResult = storageService.storeFileDual(file, folder);
            
            localUrl = storageResult.getLocalUrl();
            s3Url = storageResult.getS3Url();
            
            log.info("Stored background image: local={}, s3={}, storageType={}", 
                    localUrl, s3Url, storageType);
        } catch (Exception e) {
            log.error("Failed to store background image: {}", e.getMessage(), e);
            throw new IOException("Failed to store background image: " + e.getMessage(), e);
        }
        
        // Get image dimensions (basic implementation)
        int[] dimensions = getImageDimensions(file);
        
        // Check if this is the first image for this section
        List<BackgroundImage> existingImages = backgroundImageRepository.findBySectionName(sectionName);
        boolean isFirstImage = existingImages.isEmpty();
        
        // Create background image entity
        BackgroundImage backgroundImage = BackgroundImage.builder()
                .sectionName(sectionName)
                .imageName(imageName)
                .originalFilename(originalFilename)
                .localUrl(localUrl)
                .s3Url(s3Url)
                .storageType(storageType)
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .width(dimensions[0])
                .height(dimensions[1])
                .isActive(isFirstImage) // Auto-activate if it's the first image for this section
                .build();
        
        // Save to database
        BackgroundImage savedImage = backgroundImageRepository.save(backgroundImage);
        
        log.info("Uploaded background image: section={}, filename={}, storage={}", 
                sectionName, imageName, storageType);
        
        return savedImage;
    }
    
    // Activate a background image (deactivate others in the same section)
    public BackgroundImage activateBackgroundImage(UUID imageId) {
        BackgroundImage image = backgroundImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Background image not found with ID: " + imageId));
        
        // Deactivate all other images in the same section first
        List<BackgroundImage> sectionImages = backgroundImageRepository.findBySectionName(image.getSectionName());
        for (BackgroundImage sectionImage : sectionImages) {
            if (!sectionImage.getId().equals(imageId) && sectionImage.isActive()) {
                sectionImage.setActive(false);
                backgroundImageRepository.save(sectionImage);
                log.debug("Deactivated image: {}", sectionImage.getId());
            }
        }
        
        // Now activate the selected image
        image.setActive(true);
        BackgroundImage activatedImage = backgroundImageRepository.save(image);
        
        log.info("Activated background image: section={}, filename={}", 
                image.getSectionName(), image.getImageName());
        
        return activatedImage;
    }
    
    // Delete a background image
    public void deleteBackgroundImage(UUID imageId) {
        BackgroundImage image = backgroundImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Background image not found with ID: " + imageId));
        
        // Delete from storage
        try {
            // Extract storage key from URLs for deletion
            String s3StorageKey = extractStorageKeyFromUrl(image.getS3Url());
            String localStorageKey = extractStorageKeyFromUrl(image.getLocalUrl());
            
            if (s3StorageKey != null) {
                boolean s3Deleted = storageService.deleteFile(s3StorageKey);
                log.info("S3 file deletion result for {}: {}", s3StorageKey, s3Deleted ? "success" : "failed");
            }
            
            if (localStorageKey != null) {
                boolean localDeleted = storageService.deleteFile(localStorageKey);
                log.info("Local file deletion result for {}: {}", localStorageKey, localDeleted ? "success" : "failed");
            }
        } catch (Exception e) {
            log.warn("Failed to delete image files for ID {}: {}", imageId, e.getMessage());
        }
        
        // Delete from database
        backgroundImageRepository.delete(image);
        
        log.info("Deleted background image: section={}, filename={}", 
                image.getSectionName(), image.getImageName());
    }
    
    // Helper method to extract storage key from URL
    private String extractStorageKeyFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        // For S3 URLs: https://bucket.s3.region.amazonaws.com/path/to/file
        if (url.contains("amazonaws.com/")) {
            return url.substring(url.indexOf("amazonaws.com/") + 14);
        }
        
        // For local URLs: http://localhost:8080/uploads/path/to/file
        if (url.contains("/uploads/")) {
            return url.substring(url.indexOf("/uploads/") + 9);
        }
        
        return null;
    }
    
    // Helper methods
    private boolean isValidSection(String sectionName) {
        for (String validSection : LANDING_PAGE_SECTIONS) {
            if (validSection.equals(sectionName)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isValidMimeType(String mimeType) {
        if (mimeType == null) return false;
        for (String validType : ALLOWED_MIME_TYPES) {
            if (validType.equals(mimeType)) {
                return true;
            }
        }
        return false;
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
    
    private String getStorageType() {
        // Get from system config service
        String storageType = systemConfigService.getStorageType();
        return storageType != null ? storageType : "local";
    }
    
    private int[] getImageDimensions(MultipartFile file) {
        // Basic implementation - in production, you'd use an image library
        // For now, return default dimensions
        return new int[]{1920, 1080}; // Default hero banner size
    }
    
    // Get section information for admin panel
    public List<String> getAvailableSections() {
        return List.of(LANDING_PAGE_SECTIONS);
    }
    
    // Get allowed MIME types for admin panel
    public List<String> getAllowedMimeTypes() {
        return List.of(ALLOWED_MIME_TYPES);
    }
    
    // Check if S3 is properly configured
    public boolean isS3Configured() {
        return storageService.isS3Configured();
    }
    
    // Get storage configuration info
    public String getStorageInfo() {
        return storageService.getStorageInfo();
    }
}
