package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.CreateOrderRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.OrderDTO;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.OrderItemDTO;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.OrderResponse;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.PaymentDTO;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.UpdateOrderStatusRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Address;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Order;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.OrderItem;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Payment;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.OrderRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    
    public Page<OrderDTO> getAllOrders(Pageable pageable, String status, String customerEmail, String orderNumber) {
        Specification<Order> spec = Specification.where(null);
        
        if (status != null && !status.isEmpty()) {
            try {
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), orderStatus));
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore filter
                log.warn("Invalid order status: {}", status);
            }
        }
        
        if (customerEmail != null && !customerEmail.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("customer").get("email")), "%" + customerEmail.toLowerCase() + "%"));
        }
        
        if (orderNumber != null && !orderNumber.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.upper(root.get("orderNumber")), "%" + orderNumber.toUpperCase() + "%"));
        }
        
        Page<Order> orders = orderRepository.findAll(spec, pageable);
        return orders.map(this::convertToDTO);
    }
    
    public OrderDTO getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));
        return convertToDTO(order);
    }
    
    @Transactional
    public OrderDTO updateOrderStatus(UUID id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));
        
        order.setStatus(Order.OrderStatus.valueOf(request.getStatus()));
        order.setUpdatedAt(OffsetDateTime.now());
        
        // Note: trackingNumber and carrier fields don't exist in Order entity
        // These would need to be added to the Order entity if tracking is needed
        
        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }
        
        Order savedOrder = orderRepository.save(order);
        log.info("Updated order {} status to {}", id, request.getStatus());
        
        return convertToDTO(savedOrder);
    }
    
    public Map<String, Object> getOrderStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total orders
        long totalOrders = orderRepository.count();
        stats.put("totalOrders", totalOrders);
        
        // Orders by status
        Map<String, Long> ordersByStatus = new HashMap<>();
        for (Order.OrderStatus status : Order.OrderStatus.values()) {
            long count = orderRepository.countByStatus(status.name());
            ordersByStatus.put(status.name(), count);
        }
        stats.put("ordersByStatus", ordersByStatus);
        
        // Recent orders (last 30 days)
        OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);
        long recentOrders = orderRepository.countByCreatedAtAfter(thirtyDaysAgo);
        stats.put("recentOrders", recentOrders);
        
        // Total revenue
        List<Order> allOrders = orderRepository.findAll();
        BigDecimal totalRevenue = allOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalRevenue", totalRevenue);
        
        return stats;
    }
    
    private OrderDTO convertToDTO(Order order) {
        User user = order.getCustomer();
        
        return OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(user.getId())
                .customerEmail(user.getEmail())
                .customerName(user.getFirstName() + " " + user.getLastName())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .currency("CHF") // Default currency since Order entity doesn't have currency field
                .shippingAddress(formatAddress(order.getShippingAddress()))
                .billingAddress(formatAddress(order.getBillingAddress()))
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(convertOrderItemsToDTO(order.getOrderItems()))
                .payment(convertPaymentToDTO(order.getPayment()))
                .trackingNumber(null) // Not available in current Order entity
                .carrier(null) // Not available in current Order entity
                .build();
    }
    
    private List<OrderItemDTO> convertOrderItemsToDTO(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(item -> OrderItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productSku(item.getProduct().getSku())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))) // Calculate total price
                        .currency("CHF") // Default currency since OrderItem doesn't have currency field
                        .build())
                .collect(Collectors.toList());
    }
    
    private PaymentDTO convertPaymentToDTO(Payment payment) {
        if (payment == null) return null;
        
        return PaymentDTO.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .orderNumber(payment.getOrder().getOrderNumber())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .currency("CHF") // Default currency since Payment entity doesn't have currency field
                .transactionId(payment.getTransactionId())
                .gatewayResponse(payment.getFailureReason()) // Using failureReason as gatewayResponse
                .processedAt(payment.getProcessedAt() != null ? payment.getProcessedAt().atOffset(java.time.ZoneOffset.UTC) : null)
                .createdAt(payment.getCreatedAt() != null ? payment.getCreatedAt().atOffset(java.time.ZoneOffset.UTC) : null)
                .updatedAt(payment.getUpdatedAt() != null ? payment.getUpdatedAt().atOffset(java.time.ZoneOffset.UTC) : null)
                .customerEmail(payment.getOrder().getCustomer().getEmail())
                .customerName(payment.getOrder().getCustomer().getFirstName() + " " + payment.getOrder().getCustomer().getLastName())
                .notes(payment.getNotes())
                .build();
    }
    
    private String formatAddress(Object address) {
        // This would format the address object to a readable string
        // For now, return a placeholder
        return address != null ? address.toString() : "N/A";
    }
    
    private String formatAddressRequest(CreateOrderRequest.AddressRequest address) {
        if (address == null) return "N/A";
        
        StringBuilder sb = new StringBuilder();
        sb.append(address.getStreet());
        if (address.getApartment() != null && !address.getApartment().isEmpty()) {
            sb.append(", ").append(address.getApartment());
        }
        sb.append(", ").append(address.getCity());
        sb.append(", ").append(address.getState());
        sb.append(" ").append(address.getPostalCode());
        sb.append(", ").append(address.getCountry());
        
        return sb.toString();
    }
    
    private Address createAddressFromRequest(CreateOrderRequest.AddressRequest addressRequest, User user) {
        if (addressRequest == null) return null;
        
        return Address.builder()
                .user(user)
                .street(addressRequest.getStreet())
                .apartment(addressRequest.getApartment())
                .city(addressRequest.getCity())
                .state(addressRequest.getState())
                .postalCode(addressRequest.getPostalCode())
                .country(addressRequest.getCountry())
                .isDefault(false)
                .build();
    }
    
    /**
     * Create a new order
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String userEmail) {
        try {
            // Find user
            User user = userRepository.findByEmail(userEmail);
            if (user == null) {
                throw new UsernameNotFoundException("User not found with email: " + userEmail);
            }
            
            // Create order
            Order order = new Order();
            order.setCustomer(user);
            order.setOrderNumber(generateOrderNumber());
            order.setStatus(Order.OrderStatus.PENDING);
            order.setNotes(request.getNotes());
            
            // Set addresses - create Address entities
            order.setShippingAddress(createAddressFromRequest(request.getShippingAddress(), user));
            order.setBillingAddress(createAddressFromRequest(request.getBillingAddress(), user));
            
            // Calculate total amount (simplified)
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
                // In a real implementation, you would fetch the product and calculate price
                totalAmount = totalAmount.add(BigDecimal.valueOf(100)); // Placeholder price
            }
            order.setTotalAmount(totalAmount);
            
            Order savedOrder = orderRepository.save(order);
            
            log.info("Created order {} for user {}", savedOrder.getOrderNumber(), userEmail);
            
            return convertToOrderResponse(savedOrder);
            
        } catch (Exception e) {
            log.error("Error creating order: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create order: " + e.getMessage());
        }
    }
    
    /**
     * Get order by ID for a specific user
     */
    public OrderResponse getOrder(UUID orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        
        // Check if user owns this order or is admin
        if (!order.getCustomer().getEmail().equals(userEmail)) {
            throw new RuntimeException("Access denied: Order does not belong to user");
        }
        
        return convertToOrderResponse(order);
    }
    
    /**
     * Get user's orders
     */
    public Page<OrderResponse> getUserOrders(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail);
        		if (user == null) {
        		    throw new UsernameNotFoundException("User not found with email: " + userEmail);
        		}
        
        Page<Order> orders = orderRepository.findByCustomerOrderByOrderDateDesc(user, pageable);
        return orders.map(this::convertToOrderResponse);
    }
    
    /**
     * Cancel order
     */
    @Transactional
    public OrderResponse cancelOrder(UUID orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        
        // Check if user owns this order
        if (!order.getCustomer().getEmail().equals(userEmail)) {
            throw new RuntimeException("Access denied: Order does not belong to user");
        }
        
        // Check if order can be cancelled
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Order cannot be cancelled in current status: " + order.getStatus());
        }
        
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setUpdatedAt(OffsetDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        log.info("Cancelled order {} for user {}", savedOrder.getOrderNumber(), userEmail);
        
        return convertToOrderResponse(savedOrder);
    }
    
    /**
     * Get all orders (Admin only)
     */
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(this::convertToOrderResponse);
    }
    
    /**
     * Get orders by status (Admin only)
     */
    public Page<OrderResponse> getOrdersByStatus(Order.OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByStatusOrderByOrderDateDesc(status, pageable);
        return orders.map(this::convertToOrderResponse);
    }
    
    /**
     * Update order status (Admin only)
     */
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        
        order.setStatus(status);
        order.setUpdatedAt(OffsetDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        log.info("Updated order {} status to {}", orderId, status);
        
        return convertToOrderResponse(savedOrder);
    }
    
    /**
     * Generate unique order number
     */
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
    
    /**
     * Convert Order entity to OrderResponse DTO
     */
    private OrderResponse convertToOrderResponse(Order order) {
        User user = order.getCustomer();
        
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .taxAmount(BigDecimal.ZERO) // Placeholder
                .shippingAmount(BigDecimal.ZERO) // Placeholder
                .orderDate(order.getCreatedAt() != null ? order.getCreatedAt().toLocalDateTime() : null)
                .updatedAt(order.getUpdatedAt() != null ? order.getUpdatedAt().toLocalDateTime() : null)
                .notes(order.getNotes())
                .customer(convertToUserInfo(user))
                .shippingAddress(convertToAddressInfo(order.getShippingAddress()))
                .billingAddress(convertToAddressInfo(order.getBillingAddress()))
                .items(convertToOrderItemInfoList(order.getOrderItems()))
                .payment(convertToPaymentInfo(order.getPayment()))
                .build();
    }
    
    private OrderResponse.UserInfo convertToUserInfo(User user) {
        return OrderResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
    
    private OrderResponse.AddressInfo convertToAddressInfo(Address address) {
        if (address == null) {
            return OrderResponse.AddressInfo.builder()
                    .street("N/A")
                    .city("N/A")
                    .state("N/A")
                    .postalCode("N/A")
                    .country("N/A")
                    .build();
        }
        
        return OrderResponse.AddressInfo.builder()
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .apartment(address.getApartment())
                .build();
    }
    
    private List<OrderResponse.OrderItemInfo> convertToOrderItemInfoList(List<OrderItem> orderItems) {
        if (orderItems == null) return List.of();
        
        return orderItems.stream()
                .map(item -> OrderResponse.OrderItemInfo.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .collect(Collectors.toList());
    }
    
    private OrderResponse.PaymentInfo convertToPaymentInfo(Payment payment) {
        if (payment == null) return null;
        
        return OrderResponse.PaymentInfo.builder()
                .id(payment.getId())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .transactionId(payment.getTransactionId())
                .processedAt(payment.getProcessedAt())
                .build();
    }
}