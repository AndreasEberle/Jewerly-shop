package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.OrderDTO;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.UpdateOrderStatusRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "Admin operations for order management")
public class OrderManagementController {
    
    private final OrderService orderService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all orders with pagination and filtering")
    public ResponseEntity<Page<OrderDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerEmail,
            @RequestParam(required = false) String orderNumber) {
        try {
            Sort.Direction direction = sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sort = Sort.by(direction, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<OrderDTO> orders = orderService.getAllOrders(pageable, status, customerEmail, orderNumber);
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            log.error("Error getting orders: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable UUID id) {
        try {
            OrderDTO order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
            
        } catch (Exception e) {
            log.error("Error getting order {}: {}", id, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update order status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        try {
            OrderDTO updatedOrder = orderService.updateOrderStatus(id, request);
            return ResponseEntity.ok(updatedOrder);
            
        } catch (Exception e) {
            log.error("Error updating order {} status: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get order statistics")
    public ResponseEntity<Object> getOrderStats() {
        try {
            Object stats = orderService.getOrderStatistics();
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error getting order statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
