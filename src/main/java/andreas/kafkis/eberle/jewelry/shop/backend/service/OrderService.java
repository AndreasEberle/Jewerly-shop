package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.AddressInfo;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.CreateOrderRequest;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.OrderItemInfo;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.OrderResponse;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.PaymentInfo;
import andreas.kafkis.eberle.jewelry.shop.backend.dto.UserInfo;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Address;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Order;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.OrderItem;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Payment;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.exception.ResourceNotFoundException;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.AddressRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.OrderItemRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.OrderRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.PaymentRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.ProductRepository;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.UserRepository;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private EmailService emailService;

    /**
     * Create a new order
     */
    public OrderResponse createOrder(CreateOrderRequest request, String userEmail) {
        // Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> ResourceNotFoundException.forUser(userEmail));

        // Validate and reserve inventory
        List<OrderItem> orderItems = validateAndReserveInventory(request.getItems());

        // Create addresses
        Address shippingAddress = createAddress(request.getShippingAddress());
        Address billingAddress = createAddress(request.getBillingAddress());

        // Calculate totals
        BigDecimal subtotal = orderItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxRate = new BigDecimal("0.08"); // 8% tax
        BigDecimal taxAmount = subtotal.multiply(taxRate);
        BigDecimal shippingAmount = calculateShippingAmount(subtotal);
        BigDecimal totalAmount = subtotal.add(taxAmount).add(shippingAmount);

        // Create order
        Order order = Order.builder()
                .id(UUID.randomUUID())
                .orderNumber(generateOrderNumber())
                .customer(user)
                .status(Order.OrderStatus.PENDING)
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .shippingAmount(shippingAmount)
                .totalAmount(totalAmount)
                .shippingAddress(shippingAddress)
                .billingAddress(billingAddress)
                .notes(request.getNotes())
                .orderDate(LocalDateTime.now())
                .updatedAt(LocalDateTime.now().atOffset(java.time.ZoneOffset.UTC))
                .build();

        order = orderRepository.save(order);

        // Save order items
        for (OrderItem item : orderItems) {
            item.setOrder(order);
            orderItemRepository.save(item);
        }

        // Send order confirmation email
        emailService.sendOrderConfirmation(order);

        return convertToOrderResponse(order);
    }

    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> ResourceNotFoundException.forOrder(orderId.toString()));

        // Check if user owns this order or is admin
        if (!order.getCustomer().getEmail().equals(userEmail)) {
            throw new ResourceNotFoundException("Order not found");
        }

        return convertToOrderResponse(order);
    }

    /**
     * Get user's orders with pagination
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> ResourceNotFoundException.forUser(userEmail));

        Page<Order> orders = orderRepository.findByCustomerOrderByOrderDateDesc(user, pageable);
        return orders.map(this::convertToOrderResponse);
    }

    /**
     * Update order status (Admin only)
     */
    public OrderResponse updateOrderStatus(UUID orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> ResourceNotFoundException.forOrder(orderId.toString()));

        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now().atOffset(java.time.ZoneOffset.UTC));

        // Handle status-specific logic
        if (newStatus == Order.OrderStatus.CANCELLED && oldStatus != Order.OrderStatus.CANCELLED) {
            // Release reserved inventory
            releaseOrderInventory(order);
        } else if (newStatus == Order.OrderStatus.SHIPPED && oldStatus == Order.OrderStatus.CONFIRMED) {
            // Fulfill reserved inventory
            fulfillOrderInventory(order);
        }

        order = orderRepository.save(order);
        
        // Send status update email
        emailService.sendOrderStatusUpdate(order);
        
        return convertToOrderResponse(order);
    }

    /**
     * Cancel order
     */
    public OrderResponse cancelOrder(UUID orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> ResourceNotFoundException.forOrder(orderId.toString()));

        if (!order.getCustomer().getEmail().equals(userEmail)) {
            throw new ResourceNotFoundException("Order not found");
        }

        if (order.getStatus() == Order.OrderStatus.SHIPPED || order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Cannot cancel shipped or delivered orders");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now().atOffset(java.time.ZoneOffset.UTC));

        // Release reserved inventory
        releaseOrderInventory(order);

        order = orderRepository.save(order);
        return convertToOrderResponse(order);
    }

    /**
     * Get all orders (Admin only)
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(this::convertToOrderResponse);
    }

    /**
     * Get orders by status (Admin only)
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByStatus(Order.OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByStatusOrderByOrderDateDesc(status, pageable);
        return orders.map(this::convertToOrderResponse);
    }

    // Private helper methods

    private List<OrderItem> validateAndReserveInventory(List<CreateOrderRequest.OrderItemRequest> itemRequests) {
        return itemRequests.stream().map(itemRequest -> {
            UUID productId = UUID.fromString(itemRequest.getProductId());
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> ResourceNotFoundException.forProduct(productId.toString()));

            // Check and reserve inventory
            inventoryService.reserveStock(productId, itemRequest.getQuantity());

            return OrderItem.builder()
                    .id(UUID.randomUUID())
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(BigDecimal.valueOf(product.getPriceCents()).divide(BigDecimal.valueOf(100)))
                    .build();
        }).collect(Collectors.toList());
    }

    private Address createAddress(CreateOrderRequest.AddressRequest addressRequest) {
        Address address = Address.builder()
                .id(UUID.randomUUID())
                .street(addressRequest.getStreet())
                .apartment(addressRequest.getApartment())
                .city(addressRequest.getCity())
                .state(addressRequest.getState())
                .postalCode(addressRequest.getPostalCode())
                .country(addressRequest.getCountry())
                .build();

        return addressRepository.save(address);
    }

    private BigDecimal calculateShippingAmount(BigDecimal subtotal) {
        // Free shipping over $100
        if (subtotal.compareTo(new BigDecimal("100.00")) >= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal("9.99"); // Standard shipping
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }

    private void releaseOrderInventory(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.releaseReservedStock(item.getProduct().getId(), item.getQuantity());
        }
    }

    private void fulfillOrderInventory(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.fulfillReservedStock(item.getProduct().getId(), item.getQuantity());
        }
    }

    private OrderResponse convertToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .taxAmount(order.getTaxAmount())
                .shippingAmount(order.getShippingAmount())
                .orderDate(order.getOrderDate())
                .updatedAt(order.getUpdatedAt().toLocalDateTime())
                .notes(order.getNotes())
                .customer(UserInfo.builder()
                        .id(order.getCustomer().getId().toString())
                        .email(order.getCustomer().getEmail())
                        .firstName(order.getCustomer().getFirstName())
                        .lastName(order.getCustomer().getLastName())
                        .roles(order.getCustomer().getRoles().stream()
                                .map(role -> role.getName())
                                .collect(Collectors.toSet()))
                        .active(order.getCustomer().isActive())
                        .build())
                .shippingAddress(convertToAddressInfo(order.getShippingAddress()))
                .billingAddress(convertToAddressInfo(order.getBillingAddress()))
                .items(order.getOrderItems().stream()
                        .map(this::convertToOrderItemInfo)
                        .collect(Collectors.toList()))
                .payment(convertToPaymentInfo(order.getPayment()))
                .build();
    }

    private AddressInfo convertToAddressInfo(Address address) {
        return AddressInfo.builder()
                .street(address.getStreet())
                .apartment(address.getApartment())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .build();
    }

    private OrderItemInfo convertToOrderItemInfo(OrderItem item) {
        return OrderItemInfo.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productSku(item.getProduct().getSku())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .build();
    }

    private PaymentInfo convertToPaymentInfo(Payment payment) {
        if (payment == null) {
            return null;
        }
        return PaymentInfo.builder()
                .paymentId(payment.getId())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus().toString())
                .amount(payment.getAmount())
                .processedAt(payment.getProcessedAt())
                .build();
    }
}