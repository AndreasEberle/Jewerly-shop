package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findBySlug(String slug);
    Optional<Category> findByName(String name);
}


