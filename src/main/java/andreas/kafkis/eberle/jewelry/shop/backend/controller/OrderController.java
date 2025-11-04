package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.UUID;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.CreateOrderRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.OrderResponse;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Order;
import andreas.kafkis.eberle.jewelry.shop.backend.service.InvoiceService;
import andreas.kafkis.eberle.jewelry.shop.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final InvoiceService invoiceService;

    /**
     * Create a new order
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        String userEmail = getCurrentUserEmail();
        OrderResponse order = orderService.createOrder(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        String userEmail = getCurrentUserEmail();
        OrderResponse order = orderService.getOrder(orderId, userEmail);
        return ResponseEntity.ok(order);
    }

    /**
     * Download invoice PDF for an order
     */
    @GetMapping("/{orderId}/invoice")
    public ResponseEntity<ByteArrayResource> downloadInvoice(@PathVariable UUID orderId) {
        try {
            String userEmail = getCurrentUserEmail();
            OrderResponse orderResponse = orderService.getOrder(orderId, userEmail);
            
            // Get the order entity to generate invoice
            Order order = orderService.getOrderEntityById(orderId);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verify order belongs to user (unless admin)
            if (!order.getCustomer().getEmail().equals(userEmail) && 
                !SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                    .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            byte[] invoicePDF = invoiceService.generateInvoicePDF(order);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=Invoice_" + order.getOrderNumber() + ".pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new ByteArrayResource(invoicePDF));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get current user's orders
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        String userEmail = getCurrentUserEmail();
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<OrderResponse> orders = orderService.getUserOrders(userEmail, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Cancel order
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID orderId) {
        String userEmail = getCurrentUserEmail();
        OrderResponse order = orderService.cancelOrder(orderId, userEmail);
        return ResponseEntity.ok(order);
    }

    /**
     * Get all orders (Admin only)
     */
    @GetMapping("/admin/all")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<OrderResponse> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get orders by status (Admin only)
     */
    @GetMapping("/admin/status/{status}")
    public ResponseEntity<Page<OrderResponse>> getOrdersByStatus(
            @PathVariable Order.OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<OrderResponse> orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Update order status (Admin only)
     */
    @PutMapping("/admin/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestParam Order.OrderStatus status
    ) {
        OrderResponse order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(order);
    }
    
    /**
     * Update order tracking information (Admin only)
     */
    @PutMapping("/admin/{orderId}/tracking")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderTracking(
            @PathVariable UUID orderId,
            @RequestParam(required = false) String trackingNumber,
            @RequestParam(required = false) String carrier,
            @RequestParam(required = false) String trackingLink,
            @RequestParam(required = false) Integer estimatedDeliveryDays
    ) {
        OrderResponse order = orderService.updateOrderTracking(orderId, trackingNumber, carrier, trackingLink, estimatedDeliveryDays);
        return ResponseEntity.ok(order);
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}