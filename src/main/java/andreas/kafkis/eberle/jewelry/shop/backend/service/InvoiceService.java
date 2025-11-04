package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Order;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InvoiceService {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @Autowired
    private StorageService storageService;
    
    @Value("${email.from}")
    private String fromEmail;
    
    /**
     * Generate PDF invoice for an order
     */
    public byte[] generateInvoicePDF(Order order) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Get configurable values
            String shopName = getConfigValue("email.order_confirmation.shop_name", "Jewelry Shop");
            String companyAddress = getConfigValue("email.company.address", "");
            String supportEmail = getConfigValue("email.support.email", fromEmail);
            String supportPhone = getConfigValue("email.support.phone", "");
            
            // Fonts
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            
            // Header
            document.add(new Paragraph(shopName).setFont(boldFont).setFontSize(20).setMarginBottom(10));
            if (!companyAddress.isEmpty()) {
                document.add(new Paragraph(companyAddress).setFont(font).setFontSize(10));
            }
            if (!supportEmail.isEmpty()) {
                document.add(new Paragraph("Email: " + supportEmail).setFont(font).setFontSize(10));
            }
            if (!supportPhone.isEmpty()) {
                document.add(new Paragraph("Phone: " + supportPhone).setFont(font).setFontSize(10));
            }
            
            document.add(new Paragraph("\n").setFont(font));
            
            // Invoice title
            document.add(new Paragraph("INVOICE").setFont(boldFont).setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));
            
            // Invoice details
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
            infoTable.addCell(createCell("Invoice Number:", boldFont, true));
            infoTable.addCell(createCell(order.getOrderNumber(), font, false));
            infoTable.addCell(createCell("Invoice Date:", boldFont, true));
            infoTable.addCell(createCell(order.getOrderDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")), font, false));
            infoTable.addCell(createCell("Order Number:", boldFont, true));
            infoTable.addCell(createCell(order.getOrderNumber(), font, false));
            document.add(infoTable);
            
            document.add(new Paragraph("\n").setFont(font));
            
            // Customer information
            document.add(new Paragraph("Bill To:").setFont(boldFont).setFontSize(12).setMarginTop(10));
            String customerName = order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName();
            document.add(new Paragraph(customerName).setFont(font));
            document.add(new Paragraph(order.getCustomer().getEmail()).setFont(font));
            if (order.getBillingAddress() != null) {
                String billingAddress = formatAddress(order.getBillingAddress());
                document.add(new Paragraph(billingAddress).setFont(font));
            }
            
            document.add(new Paragraph("\n").setFont(font));
            
            // Items table
            Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1, 1})).useAllAvailableWidth();
            
            // Header row
            itemsTable.addHeaderCell(createCell("Description", boldFont, true));
            itemsTable.addHeaderCell(createCell("Quantity", boldFont, true));
            itemsTable.addHeaderCell(createCell("Unit Price", boldFont, true));
            itemsTable.addHeaderCell(createCell("Total", boldFont, true));
            
            // Item rows
            for (var item : order.getOrderItems()) {
                itemsTable.addCell(createCell(item.getProduct().getName(), font, false));
                itemsTable.addCell(createCell(String.valueOf(item.getQuantity()), font, false));
                itemsTable.addCell(createCell(formatCurrency(item.getUnitPrice(), order.getCurrency()), font, false));
                itemsTable.addCell(createCell(formatCurrency(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())), order.getCurrency()), font, false));
            }
            
            document.add(itemsTable);
            
            // Summary
            document.add(new Paragraph("\n").setFont(font));
            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth().setMarginTop(20);
            
            summaryTable.addCell(createCell("Subtotal:", font, true));
            summaryTable.addCell(createCell(formatCurrency(order.getSubtotal(), order.getCurrency()), font, false));
            
            if (order.getShippingAmount() != null && order.getShippingAmount().compareTo(BigDecimal.ZERO) > 0) {
                summaryTable.addCell(createCell("Shipping:", font, true));
                summaryTable.addCell(createCell(formatCurrency(order.getShippingAmount(), order.getCurrency()), font, false));
            }
            
            if (order.getTaxAmount() != null && order.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
                summaryTable.addCell(createCell("Tax:", font, true));
                summaryTable.addCell(createCell(formatCurrency(order.getTaxAmount(), order.getCurrency()), font, false));
            }
            
            summaryTable.addCell(createCell("Total:", boldFont, true));
            summaryTable.addCell(createCell(formatCurrency(order.getTotalAmount(), order.getCurrency()), boldFont, false));
            
            document.add(summaryTable);
            
            // Payment information
            if (order.getPayment() != null) {
                document.add(new Paragraph("\n").setFont(font));
                document.add(new Paragraph("Payment Information:").setFont(boldFont).setFontSize(12).setMarginTop(10));
                document.add(new Paragraph("Payment Method: " + order.getPayment().getPaymentMethod()).setFont(font));
                if (order.getPayment().getTransactionId() != null) {
                    document.add(new Paragraph("Transaction ID: " + order.getPayment().getTransactionId()).setFont(font));
                }
                if (order.getPayment().getProcessedAt() != null) {
                    document.add(new Paragraph("Payment Date: " + order.getPayment().getProcessedAt()
                            .format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).setFont(font));
                }
            }
            
            // Footer
            document.add(new Paragraph("\n\n").setFont(font));
            document.add(new Paragraph("Thank you for your purchase!").setFont(font).setTextAlignment(TextAlignment.CENTER));
            
            document.close();
            byte[] pdfBytes = baos.toByteArray();
            
            // Save invoice to S3 under customer-specific folder
            try {
                String fileName = "Invoice_" + order.getOrderNumber() + ".pdf";
                MultipartFile multipartFile = createMultipartFile(fileName, "application/pdf", pdfBytes);
                
                // Create customer-specific folder using email (sanitized for file path)
                String customerEmail = order.getCustomer().getEmail();
                String customerFolder = sanitizeFolderName(customerEmail);
                String folderPath = "invoices/" + customerFolder;
                
                String storageKey = storageService.storeFile(multipartFile, folderPath);
                log.info("Invoice saved to storage for customer {}: {}", customerEmail, storageKey);
            } catch (Exception e) {
                log.error("Failed to save invoice to storage: {}", e.getMessage(), e);
                // Don't fail invoice generation if storage fails
            }
            
            return pdfBytes;
            
        } catch (Exception e) {
            log.error("Error generating invoice PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }
    
    /**
     * Get invoice from storage (S3 or local)
     */
    public byte[] getInvoiceFromStorage(String orderNumber) throws IOException {
        try {
            String fileName = "Invoice_" + orderNumber + ".pdf";
            String storageKey = "invoices/" + fileName;
            // This would need to be implemented in StorageService
            // For now, we'll generate on-demand
            return null;
        } catch (Exception e) {
            log.error("Error retrieving invoice from storage: {}", e.getMessage(), e);
            return null;
        }
    }
    
    private Cell createCell(String text, PdfFont font, boolean bold) throws IOException {
        Cell cell = new Cell().add(new Paragraph(text).setFont(font));
        if (bold) {
            cell.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
        }
        return cell;
    }
    
    private String formatCurrency(BigDecimal amount, String currency) {
        return String.format("%s %.2f", currency, amount);
    }
    
    private String formatAddress(andreas.kafkis.eberle.jewelry.shop.backend.entities.Address address) {
        StringBuilder sb = new StringBuilder();
        if (address.getStreet() != null) {
            sb.append(address.getStreet());
        }
        if (address.getApartment() != null && !address.getApartment().isEmpty()) {
            sb.append(", ").append(address.getApartment());
        }
        if (address.getCity() != null) {
            sb.append("\n").append(address.getCity());
        }
        if (address.getPostalCode() != null) {
            sb.append(" ").append(address.getPostalCode());
        }
        if (address.getCountry() != null) {
            sb.append("\n").append(address.getCountry());
        }
        return sb.toString();
    }
    
    private String getConfigValue(String key, String defaultValue) {
        String value = systemConfigService.getConfigValue(key);
        return value != null && !value.isEmpty() ? value : defaultValue;
    }
    
    /**
     * Sanitize folder name from email address
     * Replaces @ with _at_ and removes invalid characters for file paths
     */
    private String sanitizeFolderName(String email) {
        if (email == null || email.isEmpty()) {
            return "unknown";
        }
        // Replace @ with _at_, remove spaces and invalid characters
        return email.toLowerCase()
                .replace("@", "_at_")
                .replaceAll("[^a-z0-9._-]", "_")
                .replaceAll("_{2,}", "_") // Replace multiple underscores with single
                .replaceAll("^_|_$", ""); // Remove leading/trailing underscores
    }
    
    /**
     * Create a MultipartFile implementation from byte array
     */
    private MultipartFile createMultipartFile(String filename, String contentType, byte[] content) {
        return new MultipartFile() {
            @Override
            public String getName() {
                return "invoice";
            }

            @Override
            public String getOriginalFilename() {
                return filename;
            }

            @Override
            public String getContentType() {
                return contentType;
            }

            @Override
            public boolean isEmpty() {
                return content == null || content.length == 0;
            }

            @Override
            public long getSize() {
                return content != null ? content.length : 0;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return content != null ? content : new byte[0];
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new java.io.ByteArrayInputStream(content != null ? content : new byte[0]);
            }

            @Override
            public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
                if (content != null) {
                    java.nio.file.Files.write(dest.toPath(), content);
                }
            }
        };
    }
    
    /**
     * Scheduled task to clean up invoices older than 1 month
     * Runs on the 1st day of every month at 2 AM
     * Cleans up invoices from all customer folders under invoices/
     */
    @Scheduled(cron = "0 0 2 1 * ?") // First day of month at 2 AM
    public void cleanupOldInvoices() {
        try {
            log.info("Starting invoice cleanup job - removing invoices older than 1 month");
            
            OffsetDateTime cutoffDate = OffsetDateTime.now().minusMonths(1);
            log.info("Invoice cleanup: Deleting invoices older than {}", cutoffDate);
            
            // This would need to be implemented in StorageService to list files by date
            // The cleanup would iterate through all customer folders under invoices/
            // For each customer folder (invoices/<customer>/), list files and delete old ones
            // Note: StorageService would need a method like:
            // List<String> customerFolders = storageService.listSubfolders("invoices");
            // for (String customerFolder : customerFolders) {
            //     List<String> oldInvoices = storageService.listFilesOlderThan(
            //         "invoices/" + customerFolder, cutoffDate);
            //     for (String invoiceKey : oldInvoices) {
            //         storageService.deleteFile(invoiceKey);
            //     }
            // }
            
            log.info("Invoice cleanup job completed (implementation needed in StorageService)");
        } catch (Exception e) {
            log.error("Error during invoice cleanup: {}", e.getMessage(), e);
        }
    }
}

