package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.CreateImageRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.UpdateImageRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.ProductImage;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductImageRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductRepository;

@RestController
@RequestMapping("/api/products")
public class ProductImageManagementController {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    public ProductImageManagementController(ProductImageRepository productImageRepository, 
                                          ProductRepository productRepository) {
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
    }

    // Create a new product image record after file upload
    @PostMapping("/{productId}/images")
    public ResponseEntity<ProductImage> createProductImage(
            @PathVariable UUID productId,
            @RequestBody CreateImageRequest request) {
        
        // Verify product exists
        if (!productRepository.existsById(productId)) {
            return ResponseEntity.notFound().build();
        }

        ProductImage image = ProductImage.builder()
                .product(productRepository.findById(productId).orElse(null))
                .storageKey(request.getStorageKey())
                .url(request.getUrl())
                .isPrimary(request.isPrimary())
                .sortOrder(request.getSortOrder())
                .altText(request.getAltText())
                .width(request.getWidth())
                .height(request.getHeight())
                .mimeType(request.getMimeType())
                .build();

        ProductImage savedImage = productImageRepository.save(image);
        return ResponseEntity.ok(savedImage);
    }

    // Update image metadata
    @PutMapping("/images/{imageId}")
    public ResponseEntity<ProductImage> updateProductImage(
            @PathVariable UUID imageId,
            @RequestBody UpdateImageRequest request) {
        
        return productImageRepository.findById(imageId)
                .map(image -> {
                    image.setAltText(request.getAltText());
                    image.setSortOrder(request.getSortOrder());
                    image.setPrimary(request.isPrimary());
                    return ResponseEntity.ok(productImageRepository.save(image));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete image
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> deleteProductImage(@PathVariable UUID imageId) {
        if (productImageRepository.existsById(imageId)) {
            productImageRepository.deleteById(imageId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Set primary image
    @PutMapping("/{productId}/images/{imageId}/primary")
    public ResponseEntity<Void> setPrimaryImage(
            @PathVariable UUID productId,
            @PathVariable UUID imageId) {
        
        List<ProductImage> images = productImageRepository.findByProductIdOrderByIsPrimaryDescSortOrderAsc(productId);
        
        // Remove primary flag from all images
        images.forEach(img -> img.setPrimary(false));
        productImageRepository.saveAll(images);
        
        // Set new primary image
        productImageRepository.findById(imageId)
                .ifPresent(img -> {
                    img.setPrimary(true);
                    productImageRepository.save(img);
                });
        
        return ResponseEntity.ok().build();
    }

}
