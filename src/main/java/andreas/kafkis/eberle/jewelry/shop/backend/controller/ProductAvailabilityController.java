package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.service.CartReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Product Availability", description = "Real-time product availability endpoints")
@Slf4j
public class ProductAvailabilityController {

    @Autowired
    private CartReservationService cartReservationService;

    @GetMapping("/availability")
    @Operation(summary = "Get availability for multiple products")
    public ResponseEntity<Map<UUID, Integer>> getAvailability(
            @RequestParam List<UUID> productIds) {
        try {
            Map<UUID, Integer> availability = productIds.stream()
                    .collect(Collectors.toMap(
                            id -> id,
                            cartReservationService::getAvailableStock
                    ));
            return ResponseEntity.ok(availability);
        } catch (Exception e) {
            log.error("Error getting product availability: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{productId}/availability")
    @Operation(summary = "Get availability for a single product")
    public ResponseEntity<Map<String, Integer>> getProductAvailability(
            @PathVariable UUID productId) {
        try {
            int available = cartReservationService.getAvailableStock(productId);
            Map<String, Integer> response = new HashMap<>();
            response.put("availableQuantity", available);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting product availability: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

