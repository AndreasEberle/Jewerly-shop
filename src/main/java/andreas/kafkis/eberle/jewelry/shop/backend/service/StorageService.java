package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.ProductImageStorageResult;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
@Slf4j
public class StorageService {

    @Autowired
    private SystemConfigService systemConfigService;


    /**
     * Store file and return the storage key
     */
    public String storeFile(MultipartFile file, String folder) throws IOException {
        String storageType = systemConfigService.getStorageType();
        log.info("Storage type detected: {}", storageType);
        
        switch (storageType.toLowerCase()) {
            case "s3":
                log.info("Using S3 storage for file: {}", file.getOriginalFilename());
                return storeFileS3(file, folder);
            case "hybrid":
                log.info("Using hybrid storage for file: {}", file.getOriginalFilename());
                return storeFileHybrid(file, folder);
            case "local":
            default:
                log.info("Using local storage for file: {}", file.getOriginalFilename());
                return storeFileLocal(file, folder);
        }
    }

    /**
     * Store file for a product using human-readable folder name
     */
    public String storeFileForProduct(MultipartFile file, String productName, String productId) throws IOException {
        String humanReadableFolder = createHumanReadableFolder(productName, productId);
        return storeFile(file, humanReadableFolder);
    }

    /**
     * Store file with dual URL storage (both local and S3)
     */
    public ProductImageStorageResult storeFileDual(MultipartFile file, String folder) throws IOException {
        String storageType = systemConfigService.getStorageType();
        ProductImageStorageResult result = new ProductImageStorageResult();
        
        try {
            switch (storageType.toLowerCase()) {
                case "s3":
                    // Store only to S3
                    String s3StorageKey = storeFileS3(file, folder);
                    String s3Url = getS3FileUrl(s3StorageKey);
                    result.setS3StorageKey(s3StorageKey);
                    result.setS3Url(s3Url);
                    result.setStorageType("s3");
                    break;
                    
                case "hybrid":
                    // Store to both S3 and local
                    String hybridS3StorageKey = storeFileS3(file, folder);
                    String hybridLocalStorageKey = storeFileLocal(file, folder);
                    String hybridS3Url = getS3FileUrl(hybridS3StorageKey);
                    String hybridLocalUrl = getLocalFileUrl(hybridLocalStorageKey);
                    result.setS3StorageKey(hybridS3StorageKey);
                    result.setLocalStorageKey(hybridLocalStorageKey);
                    result.setS3Url(hybridS3Url);
                    result.setLocalUrl(hybridLocalUrl);
                    result.setStorageType("hybrid");
                    break;
                    
                case "local":
                default:
                    // Store only to local
                    String localStorageKey = storeFileLocal(file, folder);
                    String localUrl = getLocalFileUrl(localStorageKey);
                    result.setLocalStorageKey(localStorageKey);
                    result.setLocalUrl(localUrl);
                    result.setStorageType("local");
                    break;
            }
            
            return result;
        } catch (Exception e) {
            log.error("Failed to store file with dual storage: {}", e.getMessage(), e);
            throw new IOException("Failed to store file: " + e.getMessage(), e);
        }
    }

    /**
     * Store file for a product with dual URL storage
     */
    public ProductImageStorageResult storeFileForProductDual(MultipartFile file, String productName, String productId) throws IOException {
        String humanReadableFolder = createHumanReadableFolder(productName, productId);
        return storeFileDual(file, humanReadableFolder);
    }

    /**
     * Get file URL by storage key
     */
    public String getFileUrl(String storageKey) {
        String storageType = systemConfigService.getStorageType();
        
        switch (storageType.toLowerCase()) {
            case "s3":
                return getS3FileUrl(storageKey);
            case "hybrid":
                return getFileUrlHybrid(storageKey);
            case "local":
            default:
                return getLocalFileUrl(storageKey);
        }
    }

    /**
     * Delete file by storage key
     */
    public boolean deleteFile(String storageKey) {
        String storageType = systemConfigService.getStorageType();
        
        switch (storageType.toLowerCase()) {
            case "s3":
                return deleteS3File(storageKey);
            case "local":
            default:
                return deleteLocalFile(storageKey);
        }
    }

    /**
     * Store file locally
     */
    String storeFileLocal(MultipartFile file, String folder) throws IOException {
        String basePath = systemConfigService.getLocalStoragePath();
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        String storageKey = folder + "/" + fileName;
        
        Path targetPath = Paths.get(basePath, storageKey);
        Files.createDirectories(targetPath.getParent());
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        return storageKey;
    }

    /**
     * Get local file URL
     */
    private String getLocalFileUrl(String storageKey) {
        String publicBaseUrl = systemConfigService.getPublicBaseUrl();
        return publicBaseUrl + storageKey;
    }

    /**
     * Delete local file
     */
    private boolean deleteLocalFile(String storageKey) {
        try {
            String basePath = systemConfigService.getLocalStoragePath();
            Path filePath = Paths.get(basePath, storageKey);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Store file to S3 (placeholder - needs AWS SDK implementation)
     */
    String storeFileS3(MultipartFile file, String folder) throws IOException {
        try {
            // Check if S3 is properly configured
            if (!isS3Configured()) {
                log.error("S3 is not properly configured - cannot upload file");
                throw new IOException("S3 is not properly configured");
            }
            
            S3Client s3Client = createS3Client();
            String bucketName = systemConfigService.getS3BucketName();
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            String storageKey = folder + "/" + fileName;

            log.info("Uploading file to S3 - Bucket: {}, Key: {}, Size: {}, ContentType: {}", 
                    bucketName, storageKey, file.getSize(), file.getContentType());

            // Upload file to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            log.info("Successfully uploaded file to S3: {}", storageKey);
            return storageKey;
        } catch (Exception e) {
            log.error("Failed to upload file to S3: {}", e.getMessage(), e);
            throw new IOException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    /**
     * Get S3 file URL (placeholder)
     */
    String getS3FileUrl(String storageKey) {
        String bucketName = systemConfigService.getS3BucketName();
        String region = systemConfigService.getS3Region();
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, storageKey);
    }

    /**
     * Delete S3 file (placeholder)
     */
    private boolean deleteS3File(String storageKey) {
        try {
            S3Client s3Client = createS3Client();
            String bucketName = systemConfigService.getS3BucketName();
            
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .build();
            
            s3Client.deleteObject(deleteObjectRequest);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to delete S3 file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generate human-readable unique filename
     */
    private String generateUniqueFileName(String originalFilename) {
        String extension = "";
        String baseName = "image";
        
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            baseName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        }
        
        // Create human-readable name with timestamp
        String timestamp = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
        );
        
        // Clean the base name (remove special characters, spaces, etc.)
        baseName = baseName.replaceAll("[^a-zA-Z0-9]", "-").toLowerCase();
        if (baseName.length() > 30) {
            baseName = baseName.substring(0, 30);
        }
        
        return baseName + "-" + timestamp + extension;
    }

    /**
     * Create S3 client with credentials
     */
    private S3Client createS3Client() {
        String accessKey = systemConfigService.getS3AccessKey();
        String secretKey = systemConfigService.getS3SecretKey();
        String region = systemConfigService.getS3Region();
        String bucketName = systemConfigService.getS3BucketName();
        
        log.info("S3 Configuration - Region: {}, Bucket: {}, AccessKey: {}", region, bucketName, accessKey.isEmpty() ? "EMPTY" : "SET");
        
        if (accessKey.isEmpty() || secretKey.isEmpty()) {
            log.error("S3 credentials not configured - AccessKey: {}, SecretKey: {}", accessKey.isEmpty() ? "EMPTY" : "SET", secretKey.isEmpty() ? "EMPTY" : "SET");
            throw new IllegalStateException("S3 credentials not configured");
        }

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        S3Client client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
        
        log.info("S3 client created successfully for region: {}", region);
        return client;
    }

    /**
     * Check if S3 is properly configured
     */
    public boolean isS3Configured() {
        try {
            String accessKey = systemConfigService.getS3AccessKey();
            String secretKey = systemConfigService.getS3SecretKey();
            String region = systemConfigService.getS3Region();
            String bucketName = systemConfigService.getS3BucketName();
            
            boolean configured = !accessKey.isEmpty() && !secretKey.isEmpty() && !region.isEmpty() && !bucketName.isEmpty();
            log.info("S3 Configuration check - AccessKey: {}, SecretKey: {}, Region: {}, Bucket: {}, Configured: {}", 
                    accessKey.isEmpty() ? "EMPTY" : "SET", 
                    secretKey.isEmpty() ? "EMPTY" : "SET", 
                    region, bucketName, configured);
            
            return configured;
        } catch (Exception e) {
            log.warn("Error checking S3 configuration: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Download file from S3 to local storage
     */
    public String downloadS3FileToLocal(String s3StorageKey, String localFolder) throws IOException {
        try {
            S3Client s3Client = createS3Client();
            String bucketName = systemConfigService.getS3BucketName();
            String localBasePath = systemConfigService.getLocalStoragePath();
            
            // Create local directory
            Path localDir = Paths.get(localBasePath, localFolder);
            Files.createDirectories(localDir);
            
            // Download from S3
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3StorageKey)
                    .build();
            
            String fileName = Paths.get(s3StorageKey).getFileName().toString();
            Path localFilePath = localDir.resolve(fileName);
            
            // Download the object to local file
            try (var response = s3Client.getObject(getObjectRequest)) {
                Files.copy(response, localFilePath, StandardCopyOption.REPLACE_EXISTING);
            }
            
            return localFolder + "/" + fileName;
        } catch (Exception e) {
            throw new IOException("Failed to download S3 file to local: " + e.getMessage(), e);
        }
    }

    /**
     * Get current storage configuration
     */
    public String getStorageInfo() {
        String storageType = systemConfigService.getStorageType();
        if ("s3".equals(storageType)) {
            return String.format("S3: %s/%s", 
                systemConfigService.getS3Region(), 
                systemConfigService.getS3BucketName());
        } else {
            return String.format("Local: %s", 
                systemConfigService.getLocalStoragePath());
        }
    }
    
    /**
     * Check if S3 storage is enabled
     */
    public boolean isS3Enabled() {
        String storageType = systemConfigService.getStorageType();
        return "s3".equals(storageType) || "hybrid".equals(storageType);
    }

    /**
     * Create human-readable folder name from product name
     * Example: "Classic Gold Ring" -> "products/classic-gold-ring"
     */
    private String createHumanReadableFolder(String productName, String productId) {
        if (productName == null || productName.trim().isEmpty()) {
            return "products/" + productId;
        }
        
        // Clean the product name: lowercase, replace spaces/special chars with hyphens
        String cleanName = productName.toLowerCase()
            .replaceAll("[^a-z0-9\\s]", "") // Remove special characters except spaces
            .replaceAll("\\s+", "-") // Replace spaces with hyphens
            .replaceAll("-+", "-") // Replace multiple hyphens with single hyphen
            .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens
        
        // Limit length to avoid S3 key length issues
        if (cleanName.length() > 50) {
            cleanName = cleanName.substring(0, 50);
        }
        
        return "products/" + cleanName;
    }

    /**
     * Store file in both S3 and local storage (hybrid mode)
     * Returns S3 key as primary, local key as backup
     */
    private String storeFileHybrid(MultipartFile file, String folder) throws IOException {
        try {
            // Try S3 first
            String s3Key = storeFileS3(file, folder);
            log.info("File stored in S3: {}", s3Key);
            
            // Also store locally as backup
            try {
                String localKey = storeFileLocal(file, folder);
                log.info("File also stored locally as backup: {}", localKey);
            } catch (Exception e) {
                log.warn("Failed to store file locally as backup: {}", e.getMessage());
                // Don't fail the whole operation if local backup fails
            }
            
            return s3Key; // Return S3 key as primary
        } catch (Exception e) {
            log.warn("S3 storage failed, falling back to local: {}", e.getMessage());
            // Fallback to local storage if S3 fails
            return storeFileLocal(file, folder);
        }
    }

    /**
     * Get file URL with S3 primary, local fallback (hybrid mode)
     */
    private String getFileUrlHybrid(String storageKey) {
        try {
            // Try S3 first
            String s3Url = getS3FileUrl(storageKey);
            log.debug("Using S3 URL for key: {}", storageKey);
            return s3Url;
        } catch (Exception e) {
            log.warn("S3 URL generation failed for key {}, falling back to local: {}", storageKey, e.getMessage());
            // Fallback to local URL
            return getLocalFileUrl(storageKey);
        }
    }
    
    /**
     * Clear all files from a specific S3 folder
     */
    public int clearS3Folder(String folderPath) throws IOException {
        if (!isS3Configured()) {
            throw new IOException("S3 is not configured");
        }
        
        try {
            log.info("Clearing S3 folder: {}", folderPath);
            
            S3Client s3Client = createS3Client();
            String bucketName = systemConfigService.getS3BucketName();
            
            // Ensure folder path ends with /
            if (!folderPath.endsWith("/")) {
                folderPath = folderPath + "/";
            }
            
            // List all objects with the folder prefix
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(folderPath)
                    .build();
            
            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            List<S3Object> objects = listResponse.contents();
            
            if (objects.isEmpty()) {
                log.info("No objects found in S3 folder: {}", folderPath);
                return 0;
            }
            
            // Delete all objects
            int deletedCount = 0;
            for (S3Object object : objects) {
                try {
                    DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(object.key())
                            .build();
                    
                    s3Client.deleteObject(deleteRequest);
                    deletedCount++;
                    log.debug("Deleted S3 object: {}", object.key());
                } catch (Exception e) {
                    log.error("Failed to delete S3 object {}: {}", object.key(), e.getMessage());
                }
            }
            
            log.info("Successfully deleted {} objects from S3 folder: {}", deletedCount, folderPath);
            return deletedCount;
            
        } catch (Exception e) {
            log.error("Error clearing S3 folder {}: {}", folderPath, e.getMessage(), e);
            throw new IOException("Failed to clear S3 folder: " + e.getMessage(), e);
        }
    }
    
    /**
     * Clear all files from a specific local folder
     */
    public int clearLocalFolder(String folderPath) throws IOException {
        try {
            log.info("Clearing local folder: {}", folderPath);
            
            String localStoragePath = systemConfigService.getLocalStoragePath();
            Path folder = Paths.get(localStoragePath, folderPath);
            
            if (!Files.exists(folder)) {
                log.info("Local folder does not exist: {}", folder);
                return 0;
            }
            
            if (!Files.isDirectory(folder)) {
                log.warn("Path is not a directory: {}", folder);
                return 0;
            }
            
            // Delete all files in the folder
            int deletedCount = 0;
            try (Stream<Path> paths = Files.walk(folder)) {
                List<Path> files = paths
                        .filter(Files::isRegularFile)
                        .collect(Collectors.toList());
                
                for (Path file : files) {
                    try {
                        Files.delete(file);
                        deletedCount++;
                        log.debug("Deleted local file: {}", file);
                    } catch (Exception e) {
                        log.error("Failed to delete local file {}: {}", file, e.getMessage());
                    }
                }
            }
            
            log.info("Successfully deleted {} files from local folder: {}", deletedCount, folder);
            return deletedCount;
            
        } catch (Exception e) {
            log.error("Error clearing local folder {}: {}", folderPath, e.getMessage(), e);
            throw new IOException("Failed to clear local folder: " + e.getMessage(), e);
        }
    }
    
    /**
     * Clear all files from a specific folder (S3, local, or hybrid based on storage type)
     */
    public Map<String, Object> clearFolder(String folderPath) throws IOException {
        String storageType = systemConfigService.getStorageType();
        Map<String, Object> result = new HashMap<>();
        
        log.info("Clearing folder '{}' with storage type: {}", folderPath, storageType);
        
        if ("s3".equals(storageType)) {
            int deletedCount = clearS3Folder(folderPath);
            result.put("storageType", "s3");
            result.put("deletedCount", deletedCount);
            result.put("message", "Cleared " + deletedCount + " files from S3 folder: " + folderPath);
        } else if ("hybrid".equals(storageType)) {
            // Clear both S3 and local
            int s3Deleted = 0;
            int localDeleted = 0;
            
            try {
                s3Deleted = clearS3Folder(folderPath);
            } catch (Exception e) {
                log.warn("Failed to clear S3 folder: {}", e.getMessage());
            }
            
            try {
                localDeleted = clearLocalFolder(folderPath);
            } catch (Exception e) {
                log.warn("Failed to clear local folder: {}", e.getMessage());
            }
            
            result.put("storageType", "hybrid");
            result.put("s3DeletedCount", s3Deleted);
            result.put("localDeletedCount", localDeleted);
            result.put("totalDeletedCount", s3Deleted + localDeleted);
            result.put("message", "Cleared " + s3Deleted + " files from S3 and " + localDeleted + " files from local folder: " + folderPath);
        } else {
            // Default to local
            int deletedCount = clearLocalFolder(folderPath);
            result.put("storageType", "local");
            result.put("deletedCount", deletedCount);
            result.put("message", "Cleared " + deletedCount + " files from local folder: " + folderPath);
        }
        
        return result;
    }
    
    /**
     * Copy an image from one S3 location to another
     */
    public boolean copyImage(String sourceKey, String destinationKey) {
        try {
            if (!isS3Enabled()) {
                return false;
            }
            
            S3Client s3Client = createS3Client();
            String bucketName = systemConfigService.getS3BucketName();
            
            // Copy object
            s3Client.copyObject(copyRequest -> copyRequest
                .sourceBucket(bucketName)
                .sourceKey(sourceKey)
                .destinationBucket(bucketName)
                .destinationKey(destinationKey)
            );
            
            log.info("Successfully copied image from {} to {}", sourceKey, destinationKey);
            return true;
        } catch (Exception e) {
            log.error("Error copying image from {} to {}: {}", sourceKey, destinationKey, e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete an image by key (works for both S3 and local)
     */
    public boolean deleteImage(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        
        try {
            if (isS3Enabled() && key.startsWith("products/")) {
                // S3 deletion
                return deleteFile(key);
            } else {
                // Local file deletion
                String localStoragePath = systemConfigService.getLocalStoragePath();
                Path filePath = Paths.get(localStoragePath, key);
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    log.info("Successfully deleted local image: {}", key);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("Error deleting image {}: {}", key, e.getMessage());
        }
        
        return false;
    }
}
