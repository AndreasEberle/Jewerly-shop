package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.BackgroundImage;

@Repository
public interface BackgroundImageRepository extends JpaRepository<BackgroundImage, UUID> {
    
    // Find active background image for a specific section
    Optional<BackgroundImage> findBySectionNameAndIsActiveTrue(String sectionName);
    
    // Find all background images for a specific section
    List<BackgroundImage> findBySectionName(String sectionName);
    
    // Find all background images for a specific section ordered by creation date
    List<BackgroundImage> findBySectionNameOrderByCreatedAtDesc(String sectionName);
    
    // Find all active background images
    List<BackgroundImage> findByIsActiveTrueOrderBySectionName();
    
    // Find all background images grouped by section
    @Query("SELECT b FROM BackgroundImage b ORDER BY b.sectionName, b.createdAt DESC")
    List<BackgroundImage> findAllOrderBySectionNameAndCreatedAt();
    
    // Check if there's an active image for a section
    boolean existsBySectionNameAndIsActiveTrue(String sectionName);
    
    // Count images per section
    @Query("SELECT b.sectionName, COUNT(b) FROM BackgroundImage b GROUP BY b.sectionName")
    List<Object[]> countImagesPerSection();
    
    // Find images by storage type
    List<BackgroundImage> findByStorageType(String storageType);
    
    // Find images by MIME type (for filtering GIFs, etc.)
    List<BackgroundImage> findByMimeType(String mimeType);
    
    // Find images by section and MIME type
    List<BackgroundImage> findBySectionNameAndMimeType(String sectionName, String mimeType);
}
