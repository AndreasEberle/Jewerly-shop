package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageStorageResult {
    private String localStorageKey;
    private String s3StorageKey;
    private String localUrl;
    private String s3Url;
    private String storageType;
    
    // Helper method to get the primary URL based on storage type
    public String getPrimaryUrl() {
        if ("s3".equals(storageType)) {
            return s3Url;
        } else if ("hybrid".equals(storageType)) {
            // For hybrid, prefer S3 if available, otherwise local
            return s3Url != null ? s3Url : localUrl;
        } else {
            return localUrl;
        }
    }
    
    // Helper method to get the fallback URL
    public String getFallbackUrl() {
        if ("s3".equals(storageType)) {
            return localUrl; // Fallback to local if S3 fails
        } else if ("hybrid".equals(storageType)) {
            return localUrl; // Fallback to local if S3 fails
        } else {
            return s3Url; // Fallback to S3 if local fails
        }
    }
}
