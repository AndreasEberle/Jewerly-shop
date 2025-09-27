package andreas.kafkis.eberle.jewelry.shop.backend.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "analytics_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;
    
    @Column(name = "entity_id")
    private UUID entityId; // Product ID, Category ID, etc.
    
    @Column(name = "entity_type")
    private String entityType; // "product", "category", "page", etc.
    
    @Column(name = "user_ip")
    private String userIp;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "referrer")
    private String referrer;
    
    @Column(name = "session_id")
    private String sessionId;
    
    @Column(name = "additional_data", columnDefinition = "TEXT")
    private String additionalData; // JSON string for extra data
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    public enum EventType {
        PAGE_VIEW,
        PRODUCT_VIEW,
        PRODUCT_SEARCH,
        CATEGORY_VIEW,
        FILE_UPLOAD,
        API_CALL
    }
}
