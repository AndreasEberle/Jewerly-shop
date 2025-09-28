package andreas.kafkis.eberle.jewelry.shop.backend.repository;

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

    @Modifying
    @Query("UPDATE SystemConfig sc SET sc.configValue = :value WHERE sc.configKey = :key")
    int updateConfigValue(@Param("key") String configKey, @Param("value") String configValue);

    boolean existsByConfigKey(String configKey);
}
