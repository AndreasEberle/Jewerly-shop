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

    @Value("${email.from}")
    private String fromEmail;

    @Value("${email.admin}")
    private String adminEmail;

    /**
     * Send order confirmation email
     */
    public void sendOrderConfirmation(Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(order.getCustomer().getEmail());
            helper.setSubject("Order Confirmation - " + order.getOrderNumber());

            // Prepare template context
            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("customer", order.getCustomer());
            context.setVariable("orderDate", order.getOrderDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
            context.setVariable("totalAmount", order.getTotalAmount().toString());

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
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Welcome to Jewelry Shop!");
            message.setText("Dear " + user.getFirstName() + ",\n\n" +
                    "Welcome to our jewelry shop! We're excited to have you as a customer.\n\n" +
                    "You can now browse our beautiful collection of jewelry and place orders.\n\n" +
                    "Best regards,\n" +
                    "The Jewelry Shop Team");

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
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
