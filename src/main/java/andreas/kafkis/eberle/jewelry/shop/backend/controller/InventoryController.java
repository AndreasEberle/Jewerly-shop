package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Inventory;
import andreas.kafkis.eberle.jewelry.shop.backend.service.InventoryService;

@RestController
@RequestMapping("/api/inventory")
@PreAuthorize("hasRole('ADMIN')")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    /**
     * Get all inventory records
     */
    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    /**
     * Get inventory for specific product
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<Inventory> getInventoryByProduct(@PathVariable UUID productId) {
        return inventoryService.getInventoryByProductId(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get available stock for a product
     */
    @GetMapping("/product/{productId}/available")
    public ResponseEntity<Integer> getAvailableStock(@PathVariable UUID productId) {
        int availableStock = inventoryService.getAvailableStock(productId);
        return ResponseEntity.ok(availableStock);
    }

    /**
     * Check if stock is available
     */
    @GetMapping("/product/{productId}/check")
    public ResponseEntity<Boolean> checkStockAvailability(
            @PathVariable UUID productId,
            @RequestParam int quantity
    ) {
        boolean isAvailable = inventoryService.isStockAvailable(productId, quantity);
        return ResponseEntity.ok(isAvailable);
    }

    /**
     * Add stock to inventory
     */
    @PostMapping("/product/{productId}/add")
    public ResponseEntity<Inventory> addStock(
            @PathVariable UUID productId,
            @RequestParam int quantity
    ) {
        Inventory inventory = inventoryService.addStock(productId, quantity);
        return ResponseEntity.ok(inventory);
    }

    /**
     * Remove stock from inventory
     */
    @PostMapping("/product/{productId}/remove")
    public ResponseEntity<Inventory> removeStock(
            @PathVariable UUID productId,
            @RequestParam int quantity
    ) {
        Inventory inventory = inventoryService.removeStock(productId, quantity);
        return ResponseEntity.ok(inventory);
    }

    /**
     * Set absolute stock quantity
     */
    @PutMapping("/product/{productId}")
    public ResponseEntity<Inventory> setStock(
            @PathVariable UUID productId,
            @RequestParam int quantity
    ) {
        Inventory inventory = inventoryService.setStock(productId, quantity);
        return ResponseEntity.ok(inventory);
    }

    /**
     * Reserve stock
     */
    @PostMapping("/product/{productId}/reserve")
    public ResponseEntity<Inventory> reserveStock(
            @PathVariable UUID productId,
            @RequestParam int quantity
    ) {
        try {
            Inventory inventory = inventoryService.reserveStock(productId, quantity);
            return ResponseEntity.ok(inventory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Release reserved stock
     */
    @PostMapping("/product/{productId}/release")
    public ResponseEntity<Inventory> releaseReservedStock(
            @PathVariable UUID productId,
            @RequestParam int quantity
    ) {
        try {
            Inventory inventory = inventoryService.releaseReservedStock(productId, quantity);
            return ResponseEntity.ok(inventory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Fulfill reserved stock
     */
    @PostMapping("/product/{productId}/fulfill")
    public ResponseEntity<Inventory> fulfillReservedStock(
            @PathVariable UUID productId,
            @RequestParam int quantity
    ) {
        try {
            Inventory inventory = inventoryService.fulfillReservedStock(productId, quantity);
            return ResponseEntity.ok(inventory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get products with low stock
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<Inventory>> getLowStockProducts(
            @RequestParam(defaultValue = "5") int threshold
    ) {
        List<Inventory> lowStockProducts = inventoryService.getLowStockProducts(threshold);
        return ResponseEntity.ok(lowStockProducts);
    }

    /**
     * Get out of stock products
     */
    @GetMapping("/out-of-stock")
    public ResponseEntity<List<Inventory>> getOutOfStockProducts() {
        List<Inventory> outOfStockProducts = inventoryService.getOutOfStockProducts();
        return ResponseEntity.ok(outOfStockProducts);
    }

    /**
     * Bulk stock update
     */
    @PostMapping("/bulk-update")
    public ResponseEntity<Void> bulkUpdateStock(
            @RequestBody List<InventoryService.StockUpdateRequest> updates
    ) {
        try {
            inventoryService.bulkUpdateStock(updates);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
