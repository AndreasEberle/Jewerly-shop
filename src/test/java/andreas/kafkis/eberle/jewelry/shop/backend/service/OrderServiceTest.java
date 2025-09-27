package andreas.kafkis.eberle.jewelry.shop.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import andreas.kafkis.eberle.jewelry.shop.backend.dto.OrderResponse;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Address;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Order;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.OrderItem;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private User testUser;
    private Product testProduct;
    private OrderItem testOrderItem;
    private Address testAddress;

    @BeforeEach
    void setUp() {
        // Setup User
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("customer@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        // Setup Product
        testProduct = new Product();
        testProduct.setId(UUID.randomUUID());
        testProduct.setName("Diamond Ring");
        testProduct.setSku("RING001");
        testProduct.setPriceCents(500000L); // $5000.00

        // Setup Address
        testAddress = new Address();
        testAddress.setId(UUID.randomUUID());
        testAddress.setStreet("123 Main St");
        testAddress.setCity("New York");
        testAddress.setState("NY");
        testAddress.setPostalCode("10001");
        testAddress.setCountry("USA");

        // Setup Order Item
        testOrderItem = new OrderItem();
        testOrderItem.setId(UUID.randomUUID());
        testOrderItem.setProduct(testProduct);
        testOrderItem.setQuantity(1);
        testOrderItem.setUnitPrice(BigDecimal.valueOf(5000.00));

        // Setup Order
        testOrder = new Order();
        testOrder.setId(UUID.randomUUID());
        testOrder.setOrderNumber("ORD-001");
        testOrder.setCustomer(testUser);
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setSubtotal(BigDecimal.valueOf(5000.00));
        testOrder.setTaxAmount(BigDecimal.valueOf(400.00));
        testOrder.setShippingAmount(BigDecimal.valueOf(50.00));
        testOrder.setTotalAmount(BigDecimal.valueOf(5450.00));
        testOrder.setShippingAddress(testAddress);
        testOrder.setBillingAddress(testAddress);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setCreatedAt(OffsetDateTime.now());
        testOrder.setUpdatedAt(OffsetDateTime.now());
        testOrder.setOrderItems(List.of(testOrderItem));
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder), pageable, 1);
        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        // When
        Page<OrderResponse> result = orderService.getAllOrders(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getOrderNumber()).isEqualTo("ORD-001");
    }

    @Test
    void getOrder_WhenOrderExists_ShouldReturnOrder() {
        // Given
        UUID orderId = testOrder.getId();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // When
        OrderResponse orderResponse = orderService.getOrder(orderId, "customer@example.com");

        // Then
        assertThat(orderResponse).isNotNull();
        assertThat(orderResponse.getId()).isEqualTo(orderId);
        assertThat(orderResponse.getOrderNumber()).isEqualTo("ORD-001");
    }

    @Test
    void updateOrderStatus_WhenValidStatus_ShouldUpdateOrder() {
        // Given
        UUID orderId = testOrder.getId();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        OrderResponse orderResponse = orderService.updateOrderStatus(orderId, Order.OrderStatus.CONFIRMED);

        // Then
        assertThat(orderResponse).isNotNull();
        assertThat(orderResponse.getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
        verify(orderRepository).save(any(Order.class));
        verify(emailService).sendOrderStatusUpdate(any(Order.class));
    }

    @Test
    void getOrdersByStatus_ShouldReturnOrdersWithStatus() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder), pageable, 1);
        when(orderRepository.findByStatusOrderByOrderDateDesc(Order.OrderStatus.PENDING, pageable)).thenReturn(orderPage);

        // When
        Page<OrderResponse> result = orderService.getOrdersByStatus(Order.OrderStatus.PENDING, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(Order.OrderStatus.PENDING);
    }
}
