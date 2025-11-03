package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Order;
import andreas.kafkis.eberle.jewelry.shop.backend.entities.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private SystemConfigService systemConfigService;

    @Value("${email.from}")
    private String fromEmail;

    @Value("${email.admin}")
    private String adminEmail;

    /**
     * Get config value with fallback
     */
    private String getConfigValue(String key, String defaultValue) {
        String value = systemConfigService.getConfigValue(key);
        return value != null && !value.isEmpty() ? value : defaultValue;
    }

    /**
     * Send order confirmation email with configurable values
     */
    public void sendOrderConfirmation(Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Get configurable values
            String shopName = getConfigValue("email.order_confirmation.shop_name", "Jewelry Shop");
            String estimatedDeliveryDays = getConfigValue("email.order_confirmation.estimated_delivery_days", "5-7");
            String supportEmail = getConfigValue("email.support.email", adminEmail);
            String supportPhone = getConfigValue("email.support.phone", "");
            String companyAddress = getConfigValue("email.company.address", "");
            
            // Calculate estimated delivery date
            LocalDateTime estimatedDelivery = order.getOrderDate().plusDays(Long.parseLong(estimatedDeliveryDays.split("-")[0]));
            String estimatedDeliveryDate = estimatedDelivery.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));

            helper.setFrom(fromEmail);
            helper.setTo(order.getCustomer().getEmail());
            helper.setSubject(getConfigValue("email.order_confirmation.subject", shopName + " - Order Confirmation #" + order.getOrderNumber()));

            // Prepare template context
            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("customer", order.getCustomer());
            context.setVariable("orderDate", order.getOrderDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
            context.setVariable("estimatedDeliveryDate", estimatedDeliveryDate);
            context.setVariable("estimatedDeliveryDays", estimatedDeliveryDays);
            context.setVariable("totalAmount", order.getTotalAmount());
            context.setVariable("subtotal", order.getSubtotal());
            context.setVariable("taxAmount", order.getTaxAmount());
            context.setVariable("shippingAmount", order.getShippingAmount());
            context.setVariable("shopName", shopName);
            context.setVariable("supportEmail", supportEmail);
            context.setVariable("supportPhone", supportPhone);
            context.setVariable("companyAddress", companyAddress);
            context.setVariable("footerText", getConfigValue("email.footer.text", "Thank you for shopping with us!"));

            // Generate HTML content
            String htmlContent = templateEngine.process("order-confirmation", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            // Log error but don't fail the order process
            System.err.println("Failed to send order confirmation email: " + e.getMessage());
        }
    }

    /**
     * Send order status update email
     */
    public void sendOrderStatusUpdate(Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(order.getCustomer().getEmail());
            helper.setSubject("Order Update - " + order.getOrderNumber());

            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("customer", order.getCustomer());
            context.setVariable("status", order.getStatus().toString());
            context.setVariable("updateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));

            String htmlContent = templateEngine.process("order-status-update", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send order status update email: " + e.getMessage());
        }
    }

    /**
     * Send welcome email to new user
     */
    public void sendWelcomeEmail(User user) {
        try {
            String shopName = getConfigValue("email.welcome.shop_name", "Jewelry Shop");
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject(getConfigValue("email.welcome.subject", "Welcome to " + shopName + "!"));

            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("shopName", shopName);
            context.setVariable("supportEmail", getConfigValue("email.support.email", adminEmail));

            String htmlContent = templateEngine.process("welcome-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }

    /**
     * Send account confirmation email with verification link
     */
    public void sendAccountConfirmationEmail(User user, String confirmationToken) {
        try {
            String shopName = getConfigValue("email.account_confirmation.shop_name", "Jewelry Shop");
            String baseUrl = getConfigValue("email.base_url", "http://localhost:3000");
            String confirmationLink = baseUrl + "/confirm-email?token=" + confirmationToken;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject(getConfigValue("email.account_confirmation.subject", "Please confirm your " + shopName + " account"));

            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("shopName", shopName);
            context.setVariable("confirmationLink", confirmationLink);
            context.setVariable("confirmationToken", confirmationToken);
            context.setVariable("expirationHours", getConfigValue("email.account_confirmation.token_expiration_hours", "24"));

            String htmlContent = templateEngine.process("account-confirmation", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send account confirmation email: " + e.getMessage());
        }
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(User user, String resetToken) {
        try {
            String shopName = getConfigValue("email.password_reset.shop_name", "Jewelry Shop");
            String baseUrl = getConfigValue("email.base_url", "http://localhost:3000");
            String resetLink = baseUrl + "/reset-password?token=" + resetToken;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject(getConfigValue("email.password_reset.subject", shopName + " - Password Reset Request"));

            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("shopName", shopName);
            context.setVariable("resetLink", resetLink);
            context.setVariable("resetToken", resetToken);
            context.setVariable("expirationMinutes", getConfigValue("email.password_reset.token_expiration_minutes", "60"));

            String htmlContent = templateEngine.process("password-reset", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
        }
    }

    /**
     * Send low stock alert to admin
     */
    public void sendLowStockAlert(String productName, int currentStock, int threshold) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(adminEmail);
            message.setSubject("Low Stock Alert - " + productName);
            message.setText("Product: " + productName + "\n" +
                    "Current Stock: " + currentStock + "\n" +
                    "Threshold: " + threshold + "\n\n" +
                    "Please restock this item soon.");

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send low stock alert: " + e.getMessage());
        }
    }

    /**
     * Send payment confirmation email
     */
    public void sendPaymentConfirmation(Order order, String transactionId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(order.getCustomer().getEmail());
            helper.setSubject("Payment Confirmed - " + order.getOrderNumber());

            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("customer", order.getCustomer());
            context.setVariable("transactionId", transactionId);
            context.setVariable("paymentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));

            String htmlContent = templateEngine.process("payment-confirmation", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send payment confirmation email: " + e.getMessage());
        }
    }

    /**
     * Send order cancellation email
     */
    public void sendOrderCancellation(Order order, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(order.getCustomer().getEmail());
            message.setSubject("Order Cancelled - " + order.getOrderNumber());
            message.setText("Dear " + order.getCustomer().getFirstName() + ",\n\n" +
                    "Your order " + order.getOrderNumber() + " has been cancelled.\n\n" +
                    "Reason: " + reason + "\n\n" +
                    "If you have any questions, please contact our support team.\n\n" +
                    "Best regards,\n" +
                    "The Jewelry Shop Team");

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send order cancellation email: " + e.getMessage());
        }
    }

    /**
     * Send simple text email
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}
