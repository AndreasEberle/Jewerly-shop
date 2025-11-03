package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.ProductImage;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductImageRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImageService {

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Add Amazon-style product images to a product
     * @param productSku The SKU of the product
     * @param imageUrls List of image URLs to add
     * @param isPrimary Whether the first image should be marked as primary
     */
    public void addAmazonImages(String productSku, List<String> imageUrls, boolean isPrimary) {
        Product product = productRepository.findBySku(productSku)
                .orElseThrow(() -> new RuntimeException("Product not found with SKU: " + productSku));

        log.info("Adding {} Amazon images to product: {}", imageUrls.size(), productSku);

        for (int i = 0; i < imageUrls.size(); i++) {
            String imageUrl = imageUrls.get(i);
            
            ProductImage image = ProductImage.builder()
                    .product(product)
                    .storageKey("amazon/" + productSku + "-" + (i + 1) + ".jpg")
                    .url(imageUrl)
                    .altText(product.getName() + " - Amazon Image " + (i + 1))
                    .sortOrder(i + 1)
                    .isPrimary(i == 0 && isPrimary)
                    .width(800)
                    .height(800)
                    .mimeType("image/jpeg")
                    .build();

            productImageRepository.save(image);
            log.info("Added image {} for product {}: {}", i + 1, productSku, imageUrl);
        }
    }

    /**
     * Update existing product images with Amazon URLs
     * @param productSku The SKU of the product
     * @param imageUrls List of new image URLs
     */
    public void updateWithAmazonImages(String productSku, List<String> imageUrls) {
        Product product = productRepository.findBySku(productSku)
                .orElseThrow(() -> new RuntimeException("Product not found with SKU: " + productSku));

        // Delete existing images
        List<ProductImage> existingImages = productImageRepository.findByProductOrderBySortOrder(product);
        productImageRepository.deleteAll(existingImages);
        log.info("Deleted {} existing images for product: {}", existingImages.size(), productSku);

        // Add new Amazon images
        addAmazonImages(productSku, imageUrls, true);
    }

    /**
     * Get all images for a product
     * @param productId The ID of the product
     * @return List of product images
     */
    public List<ProductImage> getProductImages(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        return productImageRepository.findByProductOrderBySortOrder(product);
    }

    /**
     * Get primary image for a product
     * @param productId The ID of the product
     * @return Primary product image or null if none exists
     */
    public ProductImage getPrimaryImage(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        return productImageRepository.findByProductAndIsPrimaryTrue(product)
                .orElse(null);
    }
}





