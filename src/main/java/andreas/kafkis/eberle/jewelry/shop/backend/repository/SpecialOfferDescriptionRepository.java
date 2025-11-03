package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.SpecialOfferDescription;

@Repository
public interface SpecialOfferDescriptionRepository extends JpaRepository<SpecialOfferDescription, UUID> {
    
    Optional<SpecialOfferDescription> findByName(String name);
    
    Optional<SpecialOfferDescription> findBySlug(String slug);
    
    @Query("SELECT s FROM SpecialOfferDescription s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<SpecialOfferDescription> findByNameContainingIgnoreCase(@Param("query") String query);
    
    @Query("SELECT s FROM SpecialOfferDescription s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<SpecialOfferDescription> findByNameOrDescriptionContainingIgnoreCase(@Param("query") String query);
    
    boolean existsByName(String name);
    
    boolean existsBySlug(String slug);
}



