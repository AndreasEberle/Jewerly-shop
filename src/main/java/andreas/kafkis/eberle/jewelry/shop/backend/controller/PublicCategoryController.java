package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Category;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.CategoryRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

@RestController
@RequestMapping("/api/public/categories")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Public Categories", description = "Public category viewing endpoints")
public class PublicCategoryController {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @GetMapping
    @Operation(summary = "Get all active categories with product counts")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(category -> {
                    CategoryDTO dto = new CategoryDTO();
                    dto.setId(category.getId().toString());
                    dto.setName(category.getName());
                    dto.setDescription(category.getDescription() != null ? category.getDescription() : "");
                    dto.setSlug(category.getSlug());
                    dto.setActive(true);
                    
                    // Count products in this category
                    long productCount = productRepository.findByActiveTrue().stream()
                            .filter(product -> product.getCategories().stream()
                                       .anyMatch(cat -> cat.getId().equals(category.getId())))
                            .count();
                    dto.setProductCount(productCount);
                    
                    // Determine if featured (categories with products are featured)
                    dto.setFeatured(productCount > 0);
                    
                    return dto;
                })
                .filter(dto -> dto.getProductCount() > 0) // Only return categories with products
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(categoryDTOs);
    }
    
    @Data
    public static class CategoryDTO {
        private String id;
        private String name;
        private String description;
        private String slug;
        private boolean active;
        private long productCount;
        private boolean featured;
    }
}

