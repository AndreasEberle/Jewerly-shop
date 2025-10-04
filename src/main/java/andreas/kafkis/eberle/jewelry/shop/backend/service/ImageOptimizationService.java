package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ImageOptimizationService {

    // Supported image formats in order of preference
    private static final List<String> PREFERRED_FORMATS = Arrays.asList("webp", "jpg", "jpeg", "png", "gif");
    
    // MIME types for each format
    private static final List<String> SUPPORTED_MIME_TYPES = Arrays.asList(
        "image/webp",
        "image/jpeg", 
        "image/jpg",
        "image/png",
        "image/gif"
    );

    /**
     * Check if the image format is supported
     */
    public boolean isSupportedFormat(String mimeType) {
        return SUPPORTED_MIME_TYPES.contains(mimeType);
    }

    /**
     * Check if the image format is optimal (WebP preferred)
     */
    public boolean isOptimalFormat(String mimeType) {
        return "image/webp".equals(mimeType);
    }

    /**
     * Get optimization recommendations for an image
     */
    public ImageOptimizationRecommendation getOptimizationRecommendation(
            String mimeType, 
            long fileSizeBytes, 
            String originalFilename) {
        
        boolean isWebP = "image/webp".equals(mimeType);
        boolean isJpeg = "image/jpeg".equals(mimeType) || "image/jpg".equals(mimeType);
        boolean isPng = "image/png".equals(mimeType);
        boolean isGif = "image/gif".equals(mimeType);
        
        double fileSizeMB = fileSizeBytes / (1024.0 * 1024.0);
        
        StringBuilder recommendations = new StringBuilder();
        int priority = 1;
        
        // WebP recommendations
        if (!isWebP) {
            recommendations.append(priority++).append(". Convert to WebP for 25-35% smaller file size\n");
        }
        
        // File size recommendations
        if (fileSizeMB > 5.0) {
            recommendations.append(priority++).append(". Compress image - current size: ").append(String.format("%.1f", fileSizeMB)).append("MB\n");
        } else if (fileSizeMB > 2.0) {
            recommendations.append(priority++).append(". Consider compressing - current size: ").append(String.format("%.1f", fileSizeMB)).append("MB\n");
        }
        
        // Format-specific recommendations
        if (isPng && !isGif) {
            recommendations.append(priority++).append(". PNG is good for graphics with transparency, but WebP is better for photos\n");
        }
        
        if (isGif) {
            recommendations.append(priority++).append(". GIF is not recommended for photos - use WebP or JPG instead\n");
        }
        
        // Quality recommendations
        if (isJpeg && fileSizeMB < 0.5) {
            recommendations.append(priority++).append(". JPEG quality might be too low - consider higher quality\n");
        }
        
        return ImageOptimizationRecommendation.builder()
                .isOptimal(isWebP && fileSizeMB <= 2.0)
                .currentFormat(mimeType)
                .recommendedFormat("image/webp")
                .fileSizeMB(fileSizeMB)
                .recommendations(recommendations.toString().trim())
                .build();
    }

    /**
     * Get the best format for a given use case
     */
    public String getBestFormatForUseCase(ImageUseCase useCase) {
        switch (useCase) {
            case PRODUCT_PHOTO:
                return "image/webp"; // Best compression for photos
            case PRODUCT_THUMBNAIL:
                return "image/webp"; // Small file size for thumbnails
            case PRODUCT_DETAIL:
                return "image/webp"; // High quality with good compression
            case LOGO_GRAPHIC:
                return "image/png"; // Better for graphics with transparency
            default:
                return "image/webp";
        }
    }

    public enum ImageUseCase {
        PRODUCT_PHOTO,
        PRODUCT_THUMBNAIL,
        PRODUCT_DETAIL,
        LOGO_GRAPHIC
    }

    // Response DTO
    public static class ImageOptimizationRecommendation {
        private boolean isOptimal;
        private String currentFormat;
        private String recommendedFormat;
        private double fileSizeMB;
        private String recommendations;

        public static ImageOptimizationRecommendationBuilder builder() {
            return new ImageOptimizationRecommendationBuilder();
        }

        // Getters and setters
        public boolean isOptimal() { return isOptimal; }
        public void setOptimal(boolean optimal) { isOptimal = optimal; }
        
        public String getCurrentFormat() { return currentFormat; }
        public void setCurrentFormat(String currentFormat) { this.currentFormat = currentFormat; }
        
        public String getRecommendedFormat() { return recommendedFormat; }
        public void setRecommendedFormat(String recommendedFormat) { this.recommendedFormat = recommendedFormat; }
        
        public double getFileSizeMB() { return fileSizeMB; }
        public void setFileSizeMB(double fileSizeMB) { this.fileSizeMB = fileSizeMB; }
        
        public String getRecommendations() { return recommendations; }
        public void setRecommendations(String recommendations) { this.recommendations = recommendations; }

        public static class ImageOptimizationRecommendationBuilder {
            private boolean isOptimal;
            private String currentFormat;
            private String recommendedFormat;
            private double fileSizeMB;
            private String recommendations;

            public ImageOptimizationRecommendationBuilder isOptimal(boolean isOptimal) {
                this.isOptimal = isOptimal;
                return this;
            }

            public ImageOptimizationRecommendationBuilder currentFormat(String currentFormat) {
                this.currentFormat = currentFormat;
                return this;
            }

            public ImageOptimizationRecommendationBuilder recommendedFormat(String recommendedFormat) {
                this.recommendedFormat = recommendedFormat;
                return this;
            }

            public ImageOptimizationRecommendationBuilder fileSizeMB(double fileSizeMB) {
                this.fileSizeMB = fileSizeMB;
                return this;
            }

            public ImageOptimizationRecommendationBuilder recommendations(String recommendations) {
                this.recommendations = recommendations;
                return this;
            }

            public ImageOptimizationRecommendation build() {
                ImageOptimizationRecommendation recommendation = new ImageOptimizationRecommendation();
                recommendation.setOptimal(isOptimal);
                recommendation.setCurrentFormat(currentFormat);
                recommendation.setRecommendedFormat(recommendedFormat);
                recommendation.setFileSizeMB(fileSizeMB);
                recommendation.setRecommendations(recommendations);
                return recommendation;
            }
        }
    }
}
