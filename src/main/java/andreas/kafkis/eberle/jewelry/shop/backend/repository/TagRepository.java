package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Tag;

public interface TagRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findBySlug(String slug);
    Optional<Tag> findByName(String name);
    List<Tag> findByNameContainingIgnoreCase(String name);
}


