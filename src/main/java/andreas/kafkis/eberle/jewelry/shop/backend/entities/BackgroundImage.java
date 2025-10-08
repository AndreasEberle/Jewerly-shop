package andreas.kafkis.eberle.jewelry.shop.backend.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "background_images")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackgroundImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "section_name", nullable = false, length = 50)
    private String sectionName;
    
    @Column(name = "image_name", nullable = false, length = 255)
    private String imageName;
    
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;
    
    @Column(name = "local_url", length = 1024)
    private String localUrl;
    
    @Column(name = "s3_url", length = 1024)
    private String s3Url;
    
    @Column(name = "storage_type", length = 20)
    @Builder.Default
    private String storageType = "local";
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "mime_type", length = 100)
    private String mimeType;
    
    @Column(name = "width")
    private Integer width;
    
    @Column(name = "height")
    private Integer height;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Helper method to get the appropriate URL based on storage type
    public String getEffectiveUrl() {
        if ("s3".equals(storageType) && s3Url != null) {
            return s3Url;
        } else if ("hybrid".equals(storageType) && s3Url != null) {
            return s3Url; // Prefer S3 in hybrid mode
        } else if (localUrl != null) {
            return localUrl;
        }
        return null;
    }
    
    // Helper method to check if image is a GIF
    public boolean isGif() {
        return mimeType != null && mimeType.equals("image/gif");
    }
    
    // Helper method to check if image is animated (for GIFs)
    public boolean isAnimated() {
        return isGif() && originalFilename.toLowerCase().contains("animated");
    }
}
