package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import andreas.kafkis.eberle.jewelry.shop.backend.dto.ValidateDiscountCodeResponse;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Address;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Cart;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Order;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.OrderItem;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Payment;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.CartRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.DiscountCodeUsageRepository;
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
    private final CartReservationService cartReservationService;
    private final andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final DiscountCodeService discountCodeService;
    private final DiscountCodeUsageRepository discountCodeUsageRepository;
    private final andreas.kafkis.eberle.jewelry.shop.backend.repository.AddressRepository addressRepository;
    private final andreas.kafkis.eberle.jewelry.shop.backend.repository.OrderItemRepository orderItemRepository;
    
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
    
    /**
     * Get order entity by ID (for internal use)
     */
    public Order getOrderEntityById(UUID id) {
        return orderRepository.findById(id).orElse(null);
    }
    
    @Transactional
    public OrderDTO updateOrderStatus(UUID id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));
        
        order.setStatus(Order.OrderStatus.valueOf(request.getStatus()));
        order.setUpdatedAt(OffsetDateTime.now());
        
        // Update tracking information if provided
        if (request.getTrackingNumber() != null && !request.getTrackingNumber().trim().isEmpty()) {
            order.setTrackingNumber(request.getTrackingNumber().trim());
        }
        if (request.getCarrier() != null && !request.getCarrier().trim().isEmpty()) {
            order.setCarrier(request.getCarrier().trim());
        }
        if (request.getTrackingLink() != null) {
            order.setTrackingLink(request.getTrackingLink().trim().isEmpty() ? null : request.getTrackingLink().trim());
        }
        if (request.getEstimatedDeliveryDays() != null) {
            order.setEstimatedDeliveryDays(request.getEstimatedDeliveryDays());
        }
        
        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }
        
        Order savedOrder = orderRepository.save(order);
        log.info("Updated order {} status to {} with tracking: {} ({}), link: {}, estimated days: {}", 
                id, request.getStatus(), request.getTrackingNumber(), request.getCarrier(), 
                request.getTrackingLink(), request.getEstimatedDeliveryDays());
        
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
            long count = orderRepository.countByStatus(status);
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
                .trackingNumber(order.getTrackingNumber())
                .carrier(order.getCarrier())
                .trackingLink(order.getTrackingLink())
                .estimatedDeliveryDays(order.getEstimatedDeliveryDays())
                .orderDate(order.getOrderDate())
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
        
        // Check if user already has this exact address (to avoid duplicates and reuse existing)
        List<Address> existingAddresses = addressRepository.findByUserId(user.getId());
        Address existingAddress = existingAddresses.stream()
            .filter(addr -> 
                addr.getStreet().equals(addressRequest.getStreet()) &&
                Objects.equals(addr.getApartment(), addressRequest.getApartment()) &&
                addr.getCity().equals(addressRequest.getCity()) &&
                Objects.equals(addr.getState(), addressRequest.getState()) &&
                Objects.equals(addr.getPostalCode(), addressRequest.getPostalCode()) &&
                addr.getCountry().equals(addressRequest.getCountry())
            )
            .findFirst()
            .orElse(null);
        
        // If address exists, reuse it
        if (existingAddress != null) {
            log.info("Reusing existing address {} for user {}", existingAddress.getId(), user.getEmail());
            return existingAddress;
        }
        
        // Create new address
        Address newAddress = Address.builder()
                .user(user)
                .street(addressRequest.getStreet())
                .apartment(addressRequest.getApartment())
                .city(addressRequest.getCity())
                .state(addressRequest.getState())
                .postalCode(addressRequest.getPostalCode())
                .country(addressRequest.getCountry())
                .isDefault(false) // Don't set as default automatically
                .build();
        
        // Save address to database (and to user's profile)
        Address savedAddress = addressRepository.save(newAddress);
        log.info("Created and saved new address {} for user {}", savedAddress.getId(), user.getEmail());
        
        return savedAddress;
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
            
            // Get user's cart to exclude its reservations from stock check
            List<Cart> userCarts = cartRepository.findByUser(user);
            UUID userCartId = userCarts.isEmpty() ? null : userCarts.get(0).getId();
            
            // Create order
            Order order = new Order();
            order.setCustomer(user);
            order.setOrderNumber(generateOrderNumber());
            order.setStatus(Order.OrderStatus.PENDING);
            order.setNotes(request.getNotes());
            
            // Set addresses - create Address entities
            order.setShippingAddress(createAddressFromRequest(request.getShippingAddress(), user));
            order.setBillingAddress(createAddressFromRequest(request.getBillingAddress(), user));
            
            // Validate stock availability and calculate total amount
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
                UUID productId = UUID.fromString(itemRequest.getProductId());
                
                // Check available stock (excluding user's own cart reservations since we're converting cart to order)
                int availableStock = userCartId != null 
                    ? cartReservationService.getAvailableStockExcludingCart(productId, userCartId)
                    : cartReservationService.getAvailableStock(productId);
                    
                if (availableStock < itemRequest.getQuantity()) {
                    throw new RuntimeException(
                        String.format("Insufficient stock for product %s. Available: %d, Requested: %d", 
                            productId, availableStock, itemRequest.getQuantity())
                    );
                }
                
                // Fetch product for price calculation
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
                
                BigDecimal itemPrice = product.isSpecialOffer() && product.getSpecialOfferPrice() != null
                        ? product.getSpecialOfferPrice()
                        : product.getPrice();
                totalAmount = totalAmount.add(itemPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            }
            
            // Apply discount code if provided
            BigDecimal discountAmount = BigDecimal.ZERO;
            if (request.getDiscountCode() != null && !request.getDiscountCode().trim().isEmpty()) {
                try {
                    ValidateDiscountCodeResponse discountResponse = discountCodeService.validateDiscountCode(
                        request.getDiscountCode().trim(), totalAmount, user);
                    
                    if (discountResponse.isValid()) {
                        discountAmount = discountResponse.getDiscountAmount();
                        totalAmount = discountResponse.getDiscountedAmount();
                        log.info("Applied discount code {} to order. Discount: {}, New total: {}", 
                            request.getDiscountCode(), discountAmount, totalAmount);
                    } else {
                        throw new RuntimeException("Invalid discount code: " + discountResponse.getMessage());
                    }
                } catch (Exception e) {
                    log.warn("Failed to apply discount code {}: {}", request.getDiscountCode(), e.getMessage());
                    throw new RuntimeException("Failed to apply discount code: " + e.getMessage());
                }
            }
            
            order.setTotalAmount(totalAmount);
            // Convert totalAmount to cents (multiply by 100 and convert to Long)
            order.setTotalCents(totalAmount.multiply(BigDecimal.valueOf(100)).longValue());
            // Set currency (default to CHF if not provided)
            order.setCurrency("CHF");
            
            // Set order date to current time
            order.setOrderDate(java.time.LocalDateTime.now());
            
            // Save order first to get ID
            Order savedOrder = orderRepository.save(order);
            
            // Create OrderItems for each product in the order
            List<OrderItem> orderItems = new java.util.ArrayList<>();
            for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
                UUID productId = UUID.fromString(itemRequest.getProductId());
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
                
                BigDecimal itemPrice = product.isSpecialOffer() && product.getSpecialOfferPrice() != null
                        ? product.getSpecialOfferPrice()
                        : product.getPrice();
                
                // Convert BigDecimal price to cents (Long)
                Long unitPriceCents = itemPrice.multiply(BigDecimal.valueOf(100)).longValue();
                
                OrderItem orderItem = OrderItem.builder()
                        .order(savedOrder)
                        .product(product)
                        .quantity(itemRequest.getQuantity())
                        .unitPriceCents(unitPriceCents)
                        .build();
                
                orderItems.add(orderItem);
            }
            
            // Save all order items
            orderItemRepository.saveAll(orderItems);
            savedOrder.getOrderItems().addAll(orderItems);
            
            // Record discount code usage if applied
            if (discountAmount.compareTo(BigDecimal.ZERO) > 0 && request.getDiscountCode() != null) {
                try {
                    discountCodeService.applyDiscountCodeToOrder(
                        request.getDiscountCode().trim(), savedOrder, user, discountAmount);
                } catch (Exception e) {
                    log.error("Failed to record discount code usage: {}", e.getMessage());
                    // Don't fail the order creation if usage tracking fails
                }
            }
            
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
        
        // Fetch orders with EntityGraph (excluding images to avoid MultipleBagFetchException)
        // Images will be loaded separately in convertToOrderItemInfoList
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
     * Update product inventory quantities based on order items
     * Decreases quantity when order is confirmed, increases when cancelled/refunded
     */
    @Transactional
    private void updateProductInventory(Order order, boolean decrease) {
        // Force load order items if lazy
        if (order.getOrderItems() != null) {
            order.getOrderItems().size(); // Trigger lazy loading
        }
        
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            log.warn("Order {} has no items, skipping inventory update", order.getId());
            return;
        }
        
        for (OrderItem item : order.getOrderItems()) {
            // Force load product if lazy
            Product product = item.getProduct();
            if (product == null) {
                log.warn("OrderItem {} has no product, skipping", item.getId());
                continue;
            }
            
            int currentQuantity = product.getQuantity() != null ? product.getQuantity() : 0;
            int quantityToUpdate = item.getQuantity();
            
            if (decrease) {
                // Decrease inventory when order is confirmed
                int newQuantity = Math.max(0, currentQuantity - quantityToUpdate);
                product.setQuantity(newQuantity);
                log.info("Decreased inventory for product {} ({}): {} -> {} (order {})", 
                    product.getName(), product.getId(), currentQuantity, newQuantity, order.getOrderNumber());
            } else {
                // Increase inventory when order is cancelled/refunded
                int newQuantity = currentQuantity + quantityToUpdate;
                product.setQuantity(newQuantity);
                log.info("Increased inventory for product {} ({}): {} -> {} (order {})", 
                    product.getName(), product.getId(), currentQuantity, newQuantity, order.getOrderNumber());
            }
            
            productRepository.save(product);
        }
    }
    
    /**
     * Update order status (Admin only)
     */
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        
        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);
        order.setUpdatedAt(OffsetDateTime.now());
        
        // Update inventory based on status change
        // If transitioning from PENDING to CONFIRMED, decrease inventory
        if (oldStatus == Order.OrderStatus.PENDING && status == Order.OrderStatus.CONFIRMED) {
            updateProductInventory(order, true); // Decrease
        }
        // If transitioning from CONFIRMED/PROCESSING/SHIPPED to CANCELLED/REFUNDED, restore inventory
        else if ((oldStatus == Order.OrderStatus.CONFIRMED || 
                  oldStatus == Order.OrderStatus.PROCESSING || 
                  oldStatus == Order.OrderStatus.SHIPPED) &&
                 (status == Order.OrderStatus.CANCELLED || status == Order.OrderStatus.REFUNDED)) {
            updateProductInventory(order, false); // Increase (restore)
        }
        
        Order savedOrder = orderRepository.save(order);
        log.info("Updated order {} status from {} to {}", orderId, oldStatus, status);
        
        return convertToOrderResponse(savedOrder);
    }
    
    /**
     * Update order tracking information (Admin only)
     */
    @Transactional
    public OrderResponse updateOrderTracking(UUID orderId, String trackingNumber, String carrier, String trackingLink, Integer estimatedDeliveryDays) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        
        if (trackingNumber != null && !trackingNumber.trim().isEmpty()) {
            order.setTrackingNumber(trackingNumber.trim());
        }
        if (carrier != null && !carrier.trim().isEmpty()) {
            order.setCarrier(carrier.trim());
        }
        if (trackingLink != null) {
            order.setTrackingLink(trackingLink.trim().isEmpty() ? null : trackingLink.trim());
        }
        if (estimatedDeliveryDays != null) {
            order.setEstimatedDeliveryDays(estimatedDeliveryDays);
        }
        order.setUpdatedAt(OffsetDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        log.info("Updated order {} tracking: {} ({}), link: {}, estimated days: {}", 
            orderId, trackingNumber, carrier, trackingLink, estimatedDeliveryDays);
        
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
        
        // Get discount code info if available
        String discountCode = null;
        BigDecimal discountAmount = BigDecimal.ZERO;
        List<andreas.kafkis.eberle.jewelry.shop.backend.entities.DiscountCodeUsage> discountUsages = 
            discountCodeUsageRepository.findByOrderId(order.getId());
        if (!discountUsages.isEmpty()) {
            andreas.kafkis.eberle.jewelry.shop.backend.entities.DiscountCodeUsage usage = discountUsages.get(0);
            discountCode = usage.getDiscountCode().getCode();
            discountAmount = usage.getDiscountAmount();
        }
        
        // Use orderDate if set, otherwise fall back to createdAt
        java.time.LocalDateTime orderDate = order.getOrderDate();
        if (orderDate == null && order.getCreatedAt() != null) {
            orderDate = order.getCreatedAt().toLocalDateTime();
        }
        
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .taxAmount(order.getTaxAmount() != null ? order.getTaxAmount() : BigDecimal.ZERO)
                .shippingAmount(order.getShippingAmount() != null ? order.getShippingAmount() : BigDecimal.ZERO)
                .orderDate(orderDate)
                .createdAt(order.getCreatedAt()) // Include createdAt directly
                .updatedAt(order.getUpdatedAt() != null ? order.getUpdatedAt().toLocalDateTime() : null)
                .notes(order.getNotes())
                .currency(order.getCurrency() != null ? order.getCurrency() : "CHF")
                .trackingNumber(order.getTrackingNumber())
                .carrier(order.getCarrier())
                .trackingLink(order.getTrackingLink())
                .estimatedDeliveryDays(order.getEstimatedDeliveryDays())
                .discountCode(discountCode)
                .discountAmount(discountAmount)
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
                .map(item -> {
                    Product product = item.getProduct();
                    // Get primary image or first image
                    // Initialize lazy collection if needed
                    String imageUrl = null;
                    try {
                        // Force load product images with eager fetch
                        if (product != null) {
                            // Try to get images - if lazy, force load them
                            List<andreas.kafkis.eberle.jewelry.shop.backend.entities.ProductImage> images = product.getImages();
                            if (images != null) {
                                // Force initialization of lazy collection
                                int size = images.size(); // This triggers lazy loading
                                if (size > 0) {
                                    // Find primary image first, then fall back to first image
                                    imageUrl = images.stream()
                                        .filter(img -> img != null && img.isPrimary())
                                        .findFirst()
                                        .map(img -> img.getUrl())
                                        .orElse(images.get(0).getUrl());
                                }
                            }
                            
                            // If still no image, try fetching product again with images using a join fetch
                            if (imageUrl == null || imageUrl.isEmpty()) {
                                try {
                                    Product productWithImages = productRepository.findById(product.getId())
                                        .orElse(null);
                                    if (productWithImages != null) {
                                        // Force load images
                                        if (productWithImages.getImages() != null) {
                                            productWithImages.getImages().size(); // Trigger lazy load
                                            List<andreas.kafkis.eberle.jewelry.shop.backend.entities.ProductImage> fetchedImages = productWithImages.getImages();
                                            if (!fetchedImages.isEmpty()) {
                                                imageUrl = fetchedImages.stream()
                                                    .filter(img -> img != null && img.isPrimary())
                                                    .findFirst()
                                                    .map(img -> img.getUrl())
                                                    .orElse(fetchedImages.get(0).getUrl());
                                            }
                                        }
                                    }
                                } catch (Exception fetchEx) {
                                    log.warn("Failed to fetch product images separately for product {}: {}", 
                                        product.getId(), fetchEx.getMessage());
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Failed to load product images for product {}: {}", 
                            product != null ? product.getId() : "unknown", e.getMessage());
                        // Continue without image
                    }
                    
                    return OrderResponse.OrderItemInfo.builder()
                            .id(item.getId())
                            .productId(product.getId())
                            .productName(product.getName())
                            .productSlug(product.getSlug())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .totalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .productImageUrl(imageUrl)
                            .build();
                })
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