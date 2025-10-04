package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.exception.ResourceNotFoundException;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.InventoryRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;

@Service
@Transactional
public class ProductService {
	
	@Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    public Product create(Product product) {
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> listAll() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Product> findAllWithPagination(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> searchProducts(
            String search,
            String category,
            String material,
            String gemstone,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean active,
            Pageable pageable
    ) {
        Specification<Product> spec = createProductSpecification(
                search, category, material, gemstone, minPrice, maxPrice, active
        );
        return productRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Product get(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forProduct(id.toString()));
    }

    @Transactional(readOnly = true)
    public List<Product> findByCategory(String categoryName) {
        return productRepository.findByCategoriesName(categoryName);
    }

    @Transactional(readOnly = true)
    public List<Product> findActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Product> findFeaturedProducts(int limit) {
        // For now, return the first N active products
        // In a real application, you might have a "featured" flag or use analytics
        return productRepository.findByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .peek(product -> {
                    // Eagerly load categories to avoid LazyInitializationException
                    product.getCategories().size();
                })
                .limit(limit)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Product> findFeaturedProducts(int limit, String targetCurrency) {
        List<Product> products = findFeaturedProducts(limit);
        
        // Convert prices to target currency if different from base currency
        if (targetCurrency != null && !targetCurrency.equals("CHF")) {
            products.forEach(product -> {
                if (!product.getBaseCurrency().equals(targetCurrency)) {
                    // This would be handled by the controller with CurrencyService
                    // For now, we just return the products as-is
                }
            });
        }
        
        return products;
    }

    /**
     * Create dynamic search specification for products
     */
    private Specification<Product> createProductSpecification(
            String search,
            String category,
            String material,
            String gemstone,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean active
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search in name, description, SKU
            if (search != null && !search.trim().isEmpty()) {
                String searchTerm = "%" + search.trim().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), searchTerm);
                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchTerm);
                Predicate skuPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("sku")), searchTerm);
                
                predicates.add(criteriaBuilder.or(namePredicate, descriptionPredicate, skuPredicate));
            }

            // Filter by category
            if (category != null && !category.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        root.join("categories").get("name"), category));
            }

            // Filter by material
            if (material != null && !material.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("material")), 
                        "%" + material.toLowerCase() + "%"));
            }

            // Filter by gemstone
            if (gemstone != null && !gemstone.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("gemstone")), 
                        "%" + gemstone.toLowerCase() + "%"));
            }

            // Filter by price range
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("priceCents"), minPrice.multiply(BigDecimal.valueOf(100)).longValue()));
            }
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("priceCents"), maxPrice.multiply(BigDecimal.valueOf(100)).longValue()));
            }

            // Filter by active status
            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}


