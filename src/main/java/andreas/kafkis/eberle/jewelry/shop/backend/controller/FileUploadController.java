package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import andreas.kafkis.eberle.jewelry.shop.backend.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin")
public class FileUploadController {

    @Value("${storage.local.base-path:}")
    private String localBasePath;
    
    @Autowired
    private AnalyticsService analyticsService;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp", "gif");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @PostMapping("/upload/{productId}")
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
}
