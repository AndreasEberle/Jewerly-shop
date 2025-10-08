package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.BrandingConfig;

@Repository
public interface BrandingConfigRepository extends JpaRepository<BrandingConfig, UUID> {
    Optional<BrandingConfig> findFirstByOrderByCreatedAtAsc();
    Optional<BrandingConfig> findByIsActiveTrue();
}
