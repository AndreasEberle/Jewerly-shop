package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.service.ProductService;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminProductOrderController {

    @Autowired
    private ProductService productService;

    /**
     * Reorder products
     */
    @PutMapping("/reorder")
    public ResponseEntity<?> reorderProducts(@RequestBody ReorderProductsRequest request) {
        try {
            System.out.println("Received reorder request: " + request.getProductIds());
            if (request.getProductIds() == null || request.getProductIds().isEmpty()) {
                return ResponseEntity.badRequest().body("Product IDs list cannot be null or empty");
            }
            productService.reorderProducts(request.getProductIds());
            return ResponseEntity.ok().body("Products reordered successfully");
        } catch (Exception e) {
            System.err.println("Error reordering products: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to reorder products: " + e.getMessage());
        }
    }

    /**
     * Get all products with their current order
     */
    @GetMapping("/order")
    public ResponseEntity<?> getProductOrder() {
        try {
            System.out.println("Getting product order...");
            List<ProductOrderDTO> products = productService.getProductOrder();
            System.out.println("Found " + products.size() + " products");
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            System.err.println("Error getting product order: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to get product order: " + e.getMessage());
        }
    }

    /**
     * Toggle featured status of a product
     */
    @PutMapping("/{productId}/toggle-featured")
    public ResponseEntity<?> toggleFeaturedStatus(@PathVariable UUID productId) {
        try {
            productService.toggleFeaturedStatus(productId);
            return ResponseEntity.ok().body("Featured status toggled successfully");
        } catch (Exception e) {
            System.err.println("Error toggling featured status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to toggle featured status: " + e.getMessage());
        }
    }

    public static class ReorderProductsRequest {
        private List<UUID> productIds;

        // Default constructor for JSON deserialization
        public ReorderProductsRequest() {}

        public List<UUID> getProductIds() {
            return productIds;
        }

        public void setProductIds(List<UUID> productIds) {
            this.productIds = productIds;
        }
    }

    public static class ProductOrderDTO {
        private UUID id;
        private String name;
        private Integer sortOrder;

        // Default constructor for JSON deserialization
        public ProductOrderDTO() {}

        public ProductOrderDTO(UUID id, String name, Integer sortOrder) {
            this.id = id;
            this.name = name;
            this.sortOrder = sortOrder;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }
    }
}
