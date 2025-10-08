package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.SectionStyle;

@Repository
public interface SectionStyleRepository extends JpaRepository<SectionStyle, UUID> {
    
    // Find active style for a specific section
    Optional<SectionStyle> findBySectionNameAndIsActiveTrue(String sectionName);
    
    // Find all styles for a specific section
    List<SectionStyle> findBySectionName(String sectionName);
    
    // Find all active styles
    List<SectionStyle> findByIsActiveTrueOrderBySectionName();
    
    // Check if there's an active style for a section
    boolean existsBySectionNameAndIsActiveTrue(String sectionName);
    
    // Find styles by section name ordered by creation date
    List<SectionStyle> findBySectionNameOrderByCreatedAtDesc(String sectionName);
}
