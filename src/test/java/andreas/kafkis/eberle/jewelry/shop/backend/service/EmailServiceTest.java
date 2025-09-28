package andreas.kafkis.eberle.jewelry.shop.backend.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Address;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Order;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.OrderItem;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Product;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.Role;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private User testUser;
    private Order testOrder;
    private Product testProduct;
    private OrderItem testOrderItem;
    private Address testAddress;

    @BeforeEach
    void setUp() {
        // Setup Role
        Role customerRole = new Role();
        
        customerRole.setName("CUSTOMER");

        // Setup User
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("andreas.kafkis.eberle@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setActive(true);
        testUser.setRoles(Set.of(customerRole));

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
        
        // TemplateEngine mock will be set up per test as needed
        
        // Set up EmailService with test configuration using reflection
        try {
            java.lang.reflect.Field fromEmailField = EmailService.class.getDeclaredField("fromEmail");
            fromEmailField.setAccessible(true);
            fromEmailField.set(emailService, "andreas.kafkis.eberle@gmail.com");
            
            java.lang.reflect.Field adminEmailField = EmailService.class.getDeclaredField("adminEmail");
            adminEmailField.setAccessible(true);
            adminEmailField.set(emailService, "andreas.kafkis.eberle@gmail.com");
        } catch (Exception e) {
            // If reflection fails, we'll just test the simple email method
        }
    }

    @Test
    void sendWelcomeEmail_ShouldSendEmail() {
        // When
        emailService.sendWelcomeEmail(testUser);

        // Then
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendOrderConfirmation_ShouldSendEmail() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(org.thymeleaf.context.Context.class)))
            .thenReturn("Test HTML content");
        
        // When
        emailService.sendOrderConfirmation(testOrder);

        // Then
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendOrderStatusUpdate_ShouldSendEmail() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(org.thymeleaf.context.Context.class)))
            .thenReturn("Test HTML content");
        
        // When
        emailService.sendOrderStatusUpdate(testOrder);

        // Then
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendPaymentConfirmation_ShouldSendEmail() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(org.thymeleaf.context.Context.class)))
            .thenReturn("Test HTML content");
        
        // When
        emailService.sendPaymentConfirmation(testOrder, "TXN-123456");

        // Then
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendSimpleEmail_ShouldSendEmail() {
        // When
        emailService.sendSimpleEmail("andreas.kafkis.eberle@gmail.com", "Test Subject", "Test Body");

        // Then
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
