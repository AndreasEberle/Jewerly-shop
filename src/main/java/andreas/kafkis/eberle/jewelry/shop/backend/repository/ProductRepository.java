package andreas.kafkis.eberle.jewelry.shop.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySku(String sku);
    Optional<Product> findBySkuIgnoreCase(String sku);
    Optional<Product> findBySlug(String slug);
    Optional<Product> findBySlugIgnoreCase(String slug);
    Optional<Product> findByName(String name);
    List<Product> findByActiveTrue();
    List<Product> findByActiveTrueOrderByCreatedAtDesc();
    List<Product> findByActiveTrueOrderBySortOrderAscCreatedAtDesc();
    List<Product> findByActiveTrueAndShowInFeaturedTrueOrderBySortOrderAscCreatedAtDesc();
    List<Product> findByCategoriesName(String categoryName);
    List<Product> findByNameIgnoreCase(String name);
}


