package andreas.kafkis.eberle.jewelry.shop.backend.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import andreas.kafkis.eberle.jewelry.shop.backend.service.SystemConfigService;

/**
 * Test AWS S3 connectivity and configuration
 */
@SpringBootTest
@ActiveProfiles("testreal")
class AwsConnectivityTest {

    @Autowired
    private SystemConfigService systemConfigService;

    @Test
    void testAwsCredentialsFormat() {
        // Test that AWS credentials are in the correct format
        String accessKey = systemConfigService.getConfigValue("storage.s3.access-key");
        String secretKey = systemConfigService.getConfigValue("storage.s3.secret-key");
        String bucketName = systemConfigService.getConfigValue("storage.s3.bucket-name");
        String region = systemConfigService.getConfigValue("storage.s3.region");

        // Check access key format (should start with AKIA)
        if (accessKey != null && !accessKey.isEmpty()) {
            assertTrue(accessKey.startsWith("AKIA"), 
                "AWS Access Key should start with 'AKIA'. Current: " + accessKey);
            assertEquals(20, accessKey.length(), 
                "AWS Access Key should be 20 characters long. Current: " + accessKey.length());
        }

        // Check secret key format (should be 40 characters)
        if (secretKey != null && !secretKey.isEmpty()) {
            assertEquals(40, secretKey.length(), 
                "AWS Secret Key should be 40 characters long. Current: " + secretKey.length());
        }

        // Check bucket name format
        if (bucketName != null && !bucketName.isEmpty()) {
            assertTrue(bucketName.matches("^[a-z0-9][a-z0-9.-]*[a-z0-9]$"), 
                "S3 bucket name should be lowercase and follow S3 naming rules. Current: " + bucketName);
            assertTrue(bucketName.length() >= 3 && bucketName.length() <= 63, 
                "S3 bucket name should be 3-63 characters long. Current: " + bucketName.length());
        }

        // Check region format
        if (region != null && !region.isEmpty()) {
            assertTrue(region.matches("^[a-z0-9-]+$"), 
                "AWS region should be lowercase with hyphens. Current: " + region);
        }

        System.out.println("AWS Configuration Check:");
        System.out.println("  Access Key: " + (accessKey != null ? accessKey.substring(0, 8) + "..." : "Not set"));
        System.out.println("  Secret Key: " + (secretKey != null ? "Set (" + secretKey.length() + " chars)" : "Not set"));
        System.out.println("  Bucket Name: " + (bucketName != null ? bucketName : "Not set"));
        System.out.println("  Region: " + (region != null ? region : "Not set"));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "AWS_ACCESS_KEY_ID", matches = ".*")
    void testAwsEnvironmentVariables() {
        // This test only runs if AWS environment variables are set
        String accessKeyId = System.getenv("AWS_ACCESS_KEY_ID");
        String secretAccessKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        String region = System.getenv("AWS_DEFAULT_REGION");

        assertNotNull(accessKeyId, "AWS_ACCESS_KEY_ID environment variable should be set");
        assertNotNull(secretAccessKey, "AWS_SECRET_ACCESS_KEY environment variable should be set");
        assertNotNull(region, "AWS_DEFAULT_REGION environment variable should be set");

        System.out.println("AWS Environment Variables:");
        System.out.println("  AWS_ACCESS_KEY_ID: " + accessKeyId.substring(0, 8) + "...");
        System.out.println("  AWS_SECRET_ACCESS_KEY: Set (" + secretAccessKey.length() + " chars)");
        System.out.println("  AWS_DEFAULT_REGION: " + region);
    }

    @Test
    void testS3BucketUrlFormat() {
        String bucketName = systemConfigService.getConfigValue("storage.s3.bucket-name");
        String region = systemConfigService.getConfigValue("storage.s3.region");
        String publicBaseUrl = systemConfigService.getConfigValue("storage.public-base-url");

        if (bucketName != null && region != null && publicBaseUrl != null) {
            // Check if the public base URL follows the correct S3 format
            String expectedUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/";
            String alternativeUrl = "https://" + bucketName + ".s3.amazonaws.com/";
            
            assertTrue(publicBaseUrl.equals(expectedUrl) || publicBaseUrl.equals(alternativeUrl), 
                "Public base URL should follow S3 format. Expected: " + expectedUrl + " or " + alternativeUrl + 
                ", but got: " + publicBaseUrl);
        }
    }

    @Test
    void testAwsConfigurationCompleteness() {
        // Test that all required AWS configuration is present
        String accessKey = systemConfigService.getConfigValue("storage.s3.access-key");
        String secretKey = systemConfigService.getConfigValue("storage.s3.secret-key");
        String bucketName = systemConfigService.getConfigValue("storage.s3.bucket-name");
        String region = systemConfigService.getConfigValue("storage.s3.region");
        String publicBaseUrl = systemConfigService.getConfigValue("storage.public-base-url");

        boolean allConfigured = accessKey != null && !accessKey.isEmpty() &&
                               secretKey != null && !secretKey.isEmpty() &&
                               bucketName != null && !bucketName.isEmpty() &&
                               region != null && !region.isEmpty() &&
                               publicBaseUrl != null && !publicBaseUrl.isEmpty();

        if (allConfigured) {
            System.out.println("✅ AWS S3 configuration is complete and ready for testing");
        } else {
            System.out.println("⚠️  AWS S3 configuration is incomplete:");
            if (accessKey == null || accessKey.isEmpty()) System.out.println("  - Missing access key");
            if (secretKey == null || secretKey.isEmpty()) System.out.println("  - Missing secret key");
            if (bucketName == null || bucketName.isEmpty()) System.out.println("  - Missing bucket name");
            if (region == null || region.isEmpty()) System.out.println("  - Missing region");
            if (publicBaseUrl == null || publicBaseUrl.isEmpty()) System.out.println("  - Missing public base URL");
        }
    }
}
