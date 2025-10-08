package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.SystemConfig;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, UUID> {
    
    Optional<SystemConfig> findByConfigKey(String configKey);
    
    Optional<SystemConfig> findByConfigKeyAndIsActiveTrue(String configKey);
    
    List<SystemConfig> findAllByConfigKey(String configKey);
    
    boolean existsByConfigKey(String configKey);
}
