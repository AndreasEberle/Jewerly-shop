package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);
}


