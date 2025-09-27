package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class StorageService {

    @Autowired
    private SystemConfigService systemConfigService;

    @Value("${storage.s3.access-key:}")
    private String s3AccessKey;

    @Value("${storage.s3.secret-key:}")
    private String s3SecretKey;

    @Value("${storage.s3.region:us-east-1}")
    private String s3Region;

    /**
     * Store file and return the storage key
     */
    public String storeFile(MultipartFile file, String folder) throws IOException {
        String storageType = systemConfigService.getStorageType();
        
        switch (storageType.toLowerCase()) {
            case "s3":
                return storeFileS3(file, folder);
            case "local":
            default:
                return storeFileLocal(file, folder);
        }
    }

    /**
     * Get file URL by storage key
     */
    public String getFileUrl(String storageKey) {
        String storageType = systemConfigService.getStorageType();
        
        switch (storageType.toLowerCase()) {
            case "s3":
                return getS3FileUrl(storageKey);
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
    private String storeFileLocal(MultipartFile file, String folder) throws IOException {
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
    private String storeFileS3(MultipartFile file, String folder) throws IOException {
        try {
            S3Client s3Client = createS3Client();
            String bucketName = systemConfigService.getS3BucketName();
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            String storageKey = folder + "/" + fileName;

            // Upload file to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            return storageKey;
        } catch (Exception e) {
            throw new IOException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

    /**
     * Get S3 file URL (placeholder)
     */
    private String getS3FileUrl(String storageKey) {
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
        if (s3AccessKey.isEmpty() || s3SecretKey.isEmpty()) {
            throw new IllegalStateException("S3 credentials not configured");
        }

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(s3AccessKey, s3SecretKey);
        
        return S3Client.builder()
                .region(Region.of(s3Region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
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
            
            s3Client.getObject(getObjectRequest, localFilePath);
            
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
}
