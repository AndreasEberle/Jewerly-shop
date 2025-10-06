package andreas.kafkis.eberle.jewelry.shop.backend.service;

import org.springframework.stereotype.Service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageOptimizationService {
    
    public ImageOptimizationRecommendation getOptimizationRecommendation(String mimeType, long fileSize, String filename) {
        ImageOptimizationRecommendation recommendation = new ImageOptimizationRecommendation();
        
        // Basic file size analysis
        if (fileSize > 5 * 1024 * 1024) { // 5MB
            recommendation.setRecommendation("File is large. Consider compressing to reduce load times.");
            recommendation.setCompressionRecommended(true);
        } else if (fileSize > 2 * 1024 * 1024) { // 2MB
            recommendation.setRecommendation("File size is moderate. Compression may improve performance.");
            recommendation.setCompressionRecommended(true);
        } else {
            recommendation.setRecommendation("File size is good for web use.");
            recommendation.setCompressionRecommended(false);
        }
        
        // Format recommendations
        if (mimeType != null) {
            if (mimeType.equals("image/webp")) {
                recommendation.setFormatRecommendation("WebP format is excellent for web use.");
            } else if (mimeType.equals("image/jpeg") || mimeType.equals("image/jpg")) {
                recommendation.setFormatRecommendation("JPEG is good. Consider converting to WebP for better compression.");
            } else if (mimeType.equals("image/png")) {
                recommendation.setFormatRecommendation("PNG is good for images with transparency. For photos, consider JPEG or WebP.");
            } else {
                recommendation.setFormatRecommendation("Consider using WebP, JPEG, or PNG for better web compatibility.");
            }
        }
        
        // Dimension recommendations (mock for now)
        recommendation.setRecommendedWidth(800);
        recommendation.setRecommendedHeight(800);
        recommendation.setCurrentFileSize(fileSize);
        recommendation.setEstimatedOptimizedSize(fileSize / 2); // Mock 50% reduction
        
        return recommendation;
    }
    
    @Data
    public static class ImageOptimizationRecommendation {
        private String recommendation;
        private String formatRecommendation;
        private boolean compressionRecommended;
        private int recommendedWidth;
        private int recommendedHeight;
        private long currentFileSize;
        private long estimatedOptimizedSize;
    }
}