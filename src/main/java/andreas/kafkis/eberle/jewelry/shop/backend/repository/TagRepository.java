package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Tag;

public interface TagRepository extends JpaRepository<Tag, Integer> {
    Optional<Tag> findBySlug(String slug);
}


