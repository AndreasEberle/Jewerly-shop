package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.SystemConfig;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, UUID> {

    Optional<SystemConfig> findByConfigKey(String configKey);
    
    List<SystemConfig> findAllByConfigKey(String configKey);
    
    Optional<SystemConfig> findByConfigKeyAndIsActiveTrue(String configKey);
    
    List<SystemConfig> findAllByIsActiveTrue();

    @Modifying
    @Query("UPDATE SystemConfig sc SET sc.configValue = :value WHERE sc.configKey = :key AND sc.isActive = true")
    int updateConfigValue(@Param("key") String configKey, @Param("value") String configValue);
    
    @Modifying
    @Query("UPDATE SystemConfig sc SET sc.isActive = false WHERE sc.configKey = :key")
    int deactivateAllByConfigKey(@Param("key") String configKey);
    
    @Modifying
    @Query("UPDATE SystemConfig sc SET sc.isActive = true WHERE sc.id = :id")
    int activateById(@Param("id") UUID id);

    boolean existsByConfigKey(String configKey);
}
