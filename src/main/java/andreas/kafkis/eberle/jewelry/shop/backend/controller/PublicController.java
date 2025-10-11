package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.service.SystemConfigService;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class PublicController {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    /**
     * Get website status and related configuration
     */
    @GetMapping("/website-status")
    public ResponseEntity<Map<String, Object>> getWebsiteStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            
            // Get website status
            String websiteStatus = systemConfigService.getConfigValue("website_status");
            status.put("status", websiteStatus != null ? websiteStatus : "normal");
            
            // Get status-specific messages and images
            if ("construction".equals(websiteStatus)) {
                String message = systemConfigService.getConfigValue("construction_message");
                String imageUrl = systemConfigService.getConfigValue("construction_image_url");
                status.put("message", message != null ? message : "We are currently working on improving our website. Please check back soon!");
                status.put("imageUrl", imageUrl);
            } else if ("vacation".equals(websiteStatus)) {
                String message = systemConfigService.getConfigValue("vacation_message");
                String imageUrl = systemConfigService.getConfigValue("vacation_image_url");
                String startDate = systemConfigService.getConfigValue("vacation_start_date");
                String endDate = systemConfigService.getConfigValue("vacation_end_date");
                
                status.put("message", message != null ? message : "We are currently on vacation and will be back soon!");
                status.put("imageUrl", imageUrl);
                status.put("startDate", startDate);
                status.put("endDate", endDate);
            }
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            // Return normal status if there's an error
            Map<String, Object> fallbackStatus = new HashMap<>();
            fallbackStatus.put("status", "normal");
            return ResponseEntity.ok(fallbackStatus);
        }
    }
}
