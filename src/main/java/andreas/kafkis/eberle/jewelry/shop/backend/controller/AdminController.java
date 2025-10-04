package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.service.MarkupConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Administrative operations")
public class AdminController {

    @Autowired
    private MarkupConfigurationService markupService;

    @GetMapping("/markup")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all markup configurations")
    public ResponseEntity<Map<String, BigDecimal>> getMarkupConfigurations() {
        return ResponseEntity.ok(markupService.getAllMarkupConfigurations());
    }

    @PostMapping("/markup")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update markup configuration")
    public ResponseEntity<String> updateMarkupConfiguration(@RequestBody MarkupUpdateRequest request) {
        try {
            markupService.setMarkupPercentage(request.getCurrency(), request.getMarkup());
            return ResponseEntity.ok("Markup updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/markup/reset")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reset all markups to defaults")
    public ResponseEntity<String> resetMarkupConfigurations() {
        markupService.resetToDefaults();
        return ResponseEntity.ok("Markups reset to defaults");
    }

    @Data
    public static class MarkupUpdateRequest {
        private String currency;
        private BigDecimal markup;
    }
}
