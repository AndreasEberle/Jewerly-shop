package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
	User findByEmail(String email);
	User findByEmailIgnoreCase(String email);
    boolean existsByEmail(String email);
}


