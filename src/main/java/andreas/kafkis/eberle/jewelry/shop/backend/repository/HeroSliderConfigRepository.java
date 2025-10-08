package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.HeroSliderConfig;

@Repository
public interface HeroSliderConfigRepository extends JpaRepository<HeroSliderConfig, UUID> {
    Optional<HeroSliderConfig> findFirstByOrderByCreatedAtAsc();
}
