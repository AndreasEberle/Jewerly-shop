package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySku(String sku);
    List<Product> findByActiveTrue();
    List<Product> findByActiveTrueOrderByCreatedAtDesc();
    List<Product> findByCategoriesName(String categoryName);
}


