package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.controller.AdminProductOrderController;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.CreateProductRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.ProductDTO;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.ProductImageDTO;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.UpdateProductRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Category;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.ProductImage;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Tag;
import andreas.kafkis.eberle.jewelry.shop.backend.exception.ResourceNotFoundException;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.CategoryRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.InventoryRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductImageRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.TagRepository;
import jakarta.persistence.criteria.Predicate;

@Service
@Transactional
public class ProductService {
	
	@Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;
    
    @Autowired
    private ProductImageRepository productImageRepository;
    
    @Autowired
    private ProductImageManagementService productImageManagementService;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private StorageService storageService;

    public Product create(Product product) {
        return productRepository.save(product);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> listAll() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product findById(UUID id) {
        return productRepository.findById(id).orElse(null);
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
        // Return only products marked as featured, ordered by sort_order, then by created_at
        return productRepository.findByActiveTrueAndShowInFeaturedTrueOrderBySortOrderAscCreatedAtDesc()
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

    /**
     * Check if a product name is unique (not used by any other product)
     */
    public boolean isProductNameUnique(String name, UUID excludeProductId) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        if (excludeProductId != null) {
            // Check if any product with different ID has this name
            return productRepository.findByNameIgnoreCase(name)
                    .stream()
                    .noneMatch(product -> !product.getId().equals(excludeProductId));
        } else {
            // Check if any product has this name
            return productRepository.findByNameIgnoreCase(name).isEmpty();
        }
    }

    /**
     * Check if a product name is unique (for new products)
     */
    public boolean isProductNameUnique(String name) {
        return isProductNameUnique(name, null);
    }
    
    /**
     * Generate SKU from product name
     */
    public String generateSku(String productName) {
        // Convert to uppercase, replace spaces and special chars with hyphens
        String baseSku = productName.toUpperCase()
                .replaceAll("[^A-Z0-9\\s]", "")
                .replaceAll("\\s+", "-");
        
        // Find the next available number
        int counter = 1;
        String sku = baseSku + "-" + String.format("%03d", counter);
        
        while (productRepository.findBySku(sku).isPresent()) {
            counter++;
            sku = baseSku + "-" + String.format("%03d", counter);
        }
        
        return sku;
    }
    
    /**
     * Create a new product with all relationships
     */
    public ProductDTO createProduct(CreateProductRequest request) {
        // Validate unique name
        if (productRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Product name already exists: " + request.getName());
        }
        
        // Validate unique SKU
        if (productRepository.findBySku(request.getSku()).isPresent()) {
            throw new IllegalArgumentException("Product SKU already exists: " + request.getSku());
        }
        
        // Create the product entity
        Product product = Product.builder()
                .name(request.getName())
                .sku(request.getSku())
                .description(request.getDescription())
                .priceCents(request.getPrice().multiply(BigDecimal.valueOf(100)).longValue())
                .baseCurrency(request.getBaseCurrency())
                .material(request.getMaterial())
                .gemstone(request.getGemstone())
                .weightGrams(request.getWeightGrams())
                .ringSize(request.getRingSize())
                .chainLength(request.getChainLength())
                .color(request.getColor())
                .finish(request.getFinish())
                .quantity(request.getQuantity())
                .active(request.isActive())
                .specialOffer(request.isSpecialOffer())
                .specialOfferPriceCents(request.getSpecialOfferPrice() != null ? 
                    request.getSpecialOfferPrice().multiply(BigDecimal.valueOf(100)).longValue() : null)
                .specialOfferDescription(request.getSpecialOfferDescription())
                .build();
        
        // Handle categories
        if (request.getCategories() != null && !request.getCategories().isEmpty()) {
            Set<Category> categories = request.getCategories().stream()
                    .map(this::findOrCreateCategory)
                    .collect(Collectors.toSet());
            product.setCategories(categories);
        }
        
        // Handle tags
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            Set<Tag> tags = request.getTags().stream()
                    .map(this::findOrCreateTag)
                    .collect(Collectors.toSet());
            product.setTags(tags);
        }
        
        // Set new product as featured with sort_order 1 and update other products
        product.setShowInFeatured(true);
        product.setSortOrder(1);
        
        // Update other products' sort_order by incrementing them
        List<Product> existingProducts = productRepository.findByActiveTrueOrderBySortOrderAscCreatedAtDesc();
        for (Product existingProduct : existingProducts) {
            existingProduct.setSortOrder(existingProduct.getSortOrder() + 1);
            productRepository.save(existingProduct);
        }
        
        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }
    
    /**
     * Update an existing product
     */
    public ProductDTO updateProduct(UUID productId, UpdateProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        // Validate unique name if being updated
        if (request.getName() != null && !request.getName().equals(product.getName())) {
            if (productRepository.findByName(request.getName()).isPresent()) {
                throw new IllegalArgumentException("Product name already exists: " + request.getName());
            }
            
            // Handle S3 image folder migration when product name changes
            try {
                productImageManagementService.handleProductNameChange(
                    productId, 
                    product.getName(), 
                    request.getName()
                );
            } catch (Exception e) {
                // Log error but don't fail the update
                System.err.println("Error migrating S3 images for product " + productId + ": " + e.getMessage());
            }
        }
        
        // Validate unique SKU if being updated
        if (request.getSku() != null && !request.getSku().equals(product.getSku())) {
            if (productRepository.findBySku(request.getSku()).isPresent()) {
                throw new IllegalArgumentException("Product SKU already exists: " + request.getSku());
            }
        }
        
        // Update basic fields
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getSku() != null) {
            product.setSku(request.getSku());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPriceCents(request.getPrice().multiply(BigDecimal.valueOf(100)).longValue());
        }
        if (request.getBaseCurrency() != null) {
            product.setBaseCurrency(request.getBaseCurrency());
        }
        if (request.getMaterial() != null) {
            product.setMaterial(request.getMaterial());
        }
        if (request.getGemstone() != null) {
            product.setGemstone(request.getGemstone());
        }
        if (request.getWeightGrams() != null) {
            product.setWeightGrams(request.getWeightGrams());
        }
        if (request.getRingSize() != null) {
            product.setRingSize(request.getRingSize());
        }
        if (request.getChainLength() != null) {
            product.setChainLength(request.getChainLength());
        }
        if (request.getColor() != null) {
            product.setColor(request.getColor());
        }
        if (request.getFinish() != null) {
            product.setFinish(request.getFinish());
        }
        if (request.getQuantity() != null) {
            product.setQuantity(request.getQuantity());
        }
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        if (request.getSpecialOffer() != null) {
            product.setSpecialOffer(request.getSpecialOffer());
        }
        if (request.getSpecialOfferPrice() != null) {
            product.setSpecialOfferPriceCents(request.getSpecialOfferPrice().multiply(BigDecimal.valueOf(100)).longValue());
        }
        if (request.getSpecialOfferDescription() != null) {
            product.setSpecialOfferDescription(request.getSpecialOfferDescription());
        }
        
        // Handle categories
        if (request.getCategories() != null) {
            Set<Category> categories = request.getCategories().stream()
                    .map(this::findOrCreateCategory)
                    .collect(Collectors.toSet());
            product.setCategories(categories);
        }
        
        // Handle tags
        if (request.getTags() != null) {
            Set<Tag> tags = request.getTags().stream()
                    .map(this::findOrCreateTag)
                    .collect(Collectors.toSet());
            product.setTags(tags);
        }
        
        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }
    
    /**
     * Get product by ID with all relationships
     */
    public ProductDTO getProductById(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return convertToDTO(product);
    }
    
    /**
     * Get all products as DTOs
     */
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get active products for public display
     */
    public List<ProductDTO> getActiveProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get featured products
     */
    public List<ProductDTO> getFeaturedProducts(int limit) {
        return productRepository.findByActiveTrueAndShowInFeaturedTrueOrderBySortOrderAscCreatedAtDesc().stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Reorder products based on provided order
     */
    @Transactional
    public void reorderProducts(List<UUID> productIds) {
        for (int i = 0; i < productIds.size(); i++) {
        	Optional<Product> product = productRepository.findById(productIds.get(i));
        	if(product.isEmpty()) {
        		throw new ResourceNotFoundException("Product not found with id: " + productIds.get(i));
        	}
            product.get().setSortOrder(i + 1);
            productRepository.save(product.get());
        }
    }

    /**
     * Get all products with their current order
     */
    @Transactional(readOnly = true)
    public List<AdminProductOrderController.ProductOrderDTO> getProductOrder() {
        return productRepository.findAll().stream()
                .map(product -> new AdminProductOrderController.ProductOrderDTO(
                        product.getId(),
                        product.getName(),
                        product.getSortOrder()
                ))
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .collect(Collectors.toList());
    }

    /**
     * Toggle featured status of a product
     */
    @Transactional
    public void toggleFeaturedStatus(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        product.setShowInFeatured(!product.getShowInFeatured());
        productRepository.save(product);
    }

    /**
     * Delete a product
     */
    public void deleteProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        // Clean up all product images (S3 and database)
        try {
            productImageManagementService.cleanupProductImages(productId);
        } catch (Exception e) {
            System.err.println("Error cleaning up product images for " + productId + ": " + e.getMessage());
            // Continue with deletion even if image cleanup fails
        }
        
        // Delete the product
        productRepository.delete(product);
    }
    
    /**
     * Find or create a category by name
     */
    private Category findOrCreateCategory(String categoryName) {
        return categoryRepository.findByName(categoryName)
                .orElseGet(() -> {
                    Category category = Category.builder()
                            .name(categoryName)
                            .slug(categoryName.toLowerCase().replaceAll("\\s+", "-"))
                            .build();
                    return categoryRepository.save(category);
                });
    }
    
    /**
     * Find or create a tag by name
     */
    private Tag findOrCreateTag(String tagName) {
        return tagRepository.findByName(tagName)
                .orElseGet(() -> {
                    Tag tag = Tag.builder()
                            .name(tagName)
                            .slug(tagName.toLowerCase().replaceAll("\\s+", "-"))
                            .build();
                    return tagRepository.save(tag);
                });
    }
    
    /**
     * Convert Product entity to ProductDTO
     */
    public ProductDTO convertToDTO(Product product) {
        // Load images for this product
        List<ProductImage> images = productImageRepository.findByProductOrderBySortOrder(product);
        
        return ProductDTO.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .baseCurrency(product.getBaseCurrency())
                .material(product.getMaterial())
                .gemstone(product.getGemstone())
                .weightGrams(product.getWeightGrams())
                .ringSize(product.getRingSize())
                .chainLength(product.getChainLength())
                .color(product.getColor())
                .finish(product.getFinish())
                .quantity(product.getQuantity())
                .active(product.isActive())
                .showInFeatured(product.getShowInFeatured())
                .specialOffer(product.isSpecialOffer())
                .specialOfferPrice(product.getSpecialOfferPrice())
                .specialOfferDescription(product.getSpecialOfferDescription())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .categories(product.getCategories().stream()
                        .map(Category::getName)
                        .collect(Collectors.toSet()))
                .tags(product.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.toSet()))
                .images(images.stream()
                        .map(this::convertImageToDTO)
                        .collect(Collectors.toList()))
                .build();
    }
    
    /**
     * Convert ProductImage entity to ProductImageDTO
     */
    private ProductImageDTO convertImageToDTO(ProductImage image) {
        return ProductImageDTO.builder()
                .id(image.getId())
                .storageKey(image.getStorageKey())
                .url(image.getUrl())
                .isPrimary(image.isPrimary())
                .sortOrder(image.getSortOrder())
                .altText(image.getAltText())
                .width(image.getWidth())
                .height(image.getHeight())
                .mimeType(image.getMimeType())
                .createdAt(image.getCreatedAt())
                .build();
    }
}


