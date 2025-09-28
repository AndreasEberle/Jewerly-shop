package andreas.kafkis.eberle.jewelry.shop.backend.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import andreas.kafkis.eberle.jewelry.shop.backend.service.StorageService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.SystemConfigService;

@SpringBootTest
@ActiveProfiles("testreal")
public class VisibleS3Test {

    @Autowired
    private StorageService storageService;

    @Autowired
    private SystemConfigService systemConfigService;
    
    @Value("${test.products.path}")
    private String testProductsPath;
    
    @Value("${storage.s3.access-key}")
    private String s3AccessKey;
    
    @Value("${storage.s3.secret-key}")
    private String s3SecretKey;
    
    @Value("${storage.s3.bucket-name}")
    private String s3BucketName;
    
    @Value("${storage.s3.region}")
    private String s3Region;

    @BeforeEach
    void setUp() {
        // Clear the downloaded-from-s3 folder before each test
        Path downloadedFromS3Dir = Paths.get("src/test/resources/test-images/downloaded-from-s3");
        if (Files.exists(downloadedFromS3Dir)) {
            try {
                Files.walk(downloadedFromS3Dir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete file: " + path + " - " + e.getMessage());
                        }
                    });
                System.out.println("🧹 Cleared downloaded-from-s3 folder");
            } catch (IOException e) {
                System.err.println("Failed to clear downloaded-from-s3 folder: " + e.getMessage());
            }
        }
    }

    @Test
    void testVisibleS3UploadAndDownload() throws IOException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🚀 DUAL UPLOAD TEST - S3 + LOCAL");
        System.out.println("=".repeat(80));

        // Find ALL .webp files in test-images directory
        Path testImagesDir = Paths.get("src/test/resources/test-images");
        if (!Files.exists(testImagesDir)) {
            System.out.println("❌ Test images directory not found: " + testImagesDir.toAbsolutePath());
            return;
        }

        // Get all .webp files
        java.util.List<Path> webpFiles = Files.walk(testImagesDir)
                .filter(path -> Files.isRegularFile(path) && path.toString().toLowerCase().endsWith(".webp"))
                .filter(path -> !path.toString().contains("downloaded-from-s3")) // Skip already downloaded files
                .collect(java.util.stream.Collectors.toList());

        if (webpFiles.isEmpty()) {
            System.out.println("❌ No .webp files found in: " + testImagesDir.toAbsolutePath());
            return;
        }

        System.out.println("📁 Found " + webpFiles.size() + " .webp files to process:");
        for (Path file : webpFiles) {
            System.out.println("   - " + file.getFileName());
        }

        // Process each file with both S3 and Local uploads
        int fileNumber = 1;
        for (Path imagePath : webpFiles) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("📸 Processing file " + fileNumber + " of " + webpFiles.size() + ": " + imagePath.getFileName());
            System.out.println("=".repeat(60));
            
            testDualUpload(imagePath, "file-" + fileNumber);
            fileNumber++;
            
            // Small delay to ensure different timestamps
            try {
                Thread.sleep(1000); // 1 second delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("\n🎉 ALL FILES PROCESSED SUCCESSFULLY!");
        System.out.println("📊 Total files processed: " + webpFiles.size());
    }
    
    private void testDualUpload(Path imagePath, String imageName) throws IOException {
        System.out.println("\n📁 Testing with: " + imagePath.getFileName());
        System.out.println("   File exists: " + Files.exists(imagePath));
        System.out.println("   File size: " + Files.size(imagePath) + " bytes");

        // Read the image file
        byte[] imageBytes = Files.readAllBytes(imagePath);
        MultipartFile testFile = new MockMultipartFile(
            "file", 
            imagePath.getFileName().toString(), 
            "image/webp", 
            imageBytes
        );

        // Configure S3
        setupS3Config();

        System.out.println("\n🔧 S3 Configuration:");
        System.out.println("   Bucket: " + s3BucketName);
        System.out.println("   Region: " + s3Region);
        System.out.println("   Access Key: " + (s3AccessKey != null ? s3AccessKey.substring(0, 8) + "..." : "null"));
        System.out.println("   Secret Key: " + (s3SecretKey != null ? s3SecretKey.substring(0, 8) + "..." : "null"));

        // ========================================
        // STEP 1: UPLOAD TO S3 + DOWNLOAD TO "downloaded-from-s3"
        // ========================================
        System.out.println("\n📤 STEP 1: S3 UPLOAD + DOWNLOAD TO 'downloaded-from-s3'");
        
        // Upload to S3
        String s3Key = storageService.storeFile(testFile, "test-images");
        System.out.println("✅ S3 Upload Result:");
        System.out.println("   S3 Key: " + s3Key);
        System.out.println("   S3 URL: " + storageService.getFileUrl(s3Key));
        
        // Download from S3 directly to src/test/resources/downloaded-from-s3
        downloadS3ToTestResources(s3Key, imagePath.getFileName().toString());

        // ========================================
        // STEP 2: UPLOAD TO LOCAL + DOWNLOAD TO "products-test"
        // ========================================
        System.out.println("\n📤 STEP 2: LOCAL UPLOAD + DOWNLOAD TO 'products-test'");
        
        // Switch to local storage
        systemConfigService.updateConfig("USE_S3_STORAGE", "false");
        
        // Upload to local storage (but don't create uploads folders)
        String localKey = storageService.storeFile(testFile, "local-test");
        System.out.println("✅ Local Upload Result:");
        System.out.println("   Local Key: " + localKey);
        System.out.println("   Local URL: " + storageService.getFileUrl(localKey));
        
        // Download to products-test folder (copy from the uploaded file)
        downloadToProductsTest(localKey, imagePath.getFileName().toString());
        
        System.out.println("\n🎯 SUMMARY for " + imageName + ":");
        System.out.println("   1. S3: jewelry-shop-images/test-images/ → downloaded-from-s3/");
        System.out.println("   2. Local: uploads/local-test/ → products-test/");
        System.out.println("   3. Check both download locations!");

        // Don't clean up so you can see the files
        System.out.println("\n⚠️  Files NOT cleaned up so you can see them!");
    }
    
    private void setupS3Config() {
        // Set storage type to S3 in the database for the StorageService
        try {
            systemConfigService.updateConfig("USE_S3_STORAGE", "true");
        } catch (Exception e) {
            systemConfigService.createConfig("USE_S3_STORAGE", "true", "Use S3 storage instead of local storage");
        }
        
        // Set S3 configuration in the database for the StorageService
        try {
            systemConfigService.updateConfig("S3_BUCKET_NAME", s3BucketName);
        } catch (Exception e) {
            systemConfigService.createConfig("S3_BUCKET_NAME", s3BucketName, "S3 bucket name");
        }
        
        try {
            systemConfigService.updateConfig("S3_REGION", s3Region);
        } catch (Exception e) {
            systemConfigService.createConfig("S3_REGION", s3Region, "S3 region");
        }
        
        // Set local storage path for when we switch to local storage
        try {
            systemConfigService.updateConfig("LOCAL_STORAGE_PATH", "uploads/");
        } catch (Exception e) {
            systemConfigService.createConfig("LOCAL_STORAGE_PATH", "uploads/", "Local storage path");
        }
        
        // Set public base URL for local storage
        try {
            systemConfigService.updateConfig("PUBLIC_BASE_URL", "http://localhost:8080/");
        } catch (Exception e) {
            systemConfigService.createConfig("PUBLIC_BASE_URL", "http://localhost:8080/", "Public base URL");
        }
    }
    
    private void downloadS3ToTestResources(String s3Key, String originalFileName) throws IOException {
        // Create downloaded-from-s3 directory inside test-images
        Path downloadedFromS3Dir = Paths.get("src/test/resources/test-images/downloaded-from-s3");
        Files.createDirectories(downloadedFromS3Dir);
        
        // Generate human-readable filename for download
        String baseName = originalFileName.substring(0, originalFileName.lastIndexOf("."));
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String timestamp = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
        );
        String humanReadableName = baseName + "-" + timestamp + extension;
        
        Path downloadedFile = downloadedFromS3Dir.resolve(humanReadableName);
        
        // Download directly from S3 to test resources
        try {
            // Use S3 client directly to download to test resources
            software.amazon.awssdk.services.s3.S3Client s3Client = createS3Client();
            String bucketName = s3BucketName;
            
            software.amazon.awssdk.services.s3.model.GetObjectRequest getObjectRequest = 
                software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            
            s3Client.getObject(getObjectRequest, downloadedFile);
            
            System.out.println("✅ S3 Download Result:");
            System.out.println("   Downloaded to: " + downloadedFile.toAbsolutePath());
            System.out.println("   File exists: " + Files.exists(downloadedFile));
            System.out.println("   File size: " + Files.size(downloadedFile) + " bytes");
            
        } catch (Exception e) {
            System.out.println("❌ S3 download failed: " + e.getMessage());
            throw new IOException("Failed to download from S3", e);
        }
    }
    
    private software.amazon.awssdk.services.s3.S3Client createS3Client() {
        String region = s3Region;
        
        software.amazon.awssdk.auth.credentials.AwsBasicCredentials awsCredentials = 
            software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create(s3AccessKey, s3SecretKey);
        
        return software.amazon.awssdk.services.s3.S3Client.builder()
            .region(software.amazon.awssdk.regions.Region.of(region))
            .credentialsProvider(software.amazon.awssdk.auth.credentials.StaticCredentialsProvider.create(awsCredentials))
            .build();
    }
    
    private void downloadToProductsTest(String localKey, String originalFileName) throws IOException {
        // Create products-test directory
        Path productsTestDir = Paths.get(testProductsPath);
        Files.createDirectories(productsTestDir);
        
        // Generate human-readable filename for download
        String baseName = originalFileName.substring(0, originalFileName.lastIndexOf("."));
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String timestamp = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
        );
        String humanReadableName = baseName + "-" + timestamp + extension;
        
        Path downloadedFile = productsTestDir.resolve(humanReadableName);
        
        // Find the local uploaded file
        String localBasePath = systemConfigService.getLocalStoragePath();
        Path sourceFile = Paths.get(localBasePath, localKey);
        
        if (Files.exists(sourceFile)) {
            // Copy to products-test folder
            Files.copy(sourceFile, downloadedFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ Local Download Result:");
            System.out.println("   Downloaded to: " + downloadedFile.toAbsolutePath());
            System.out.println("   File exists: " + Files.exists(downloadedFile));
            System.out.println("   File size: " + Files.size(downloadedFile) + " bytes");
        } else {
            System.out.println("❌ Source file not found: " + sourceFile.toAbsolutePath());
        }
    }
}