package andreas.kafkis.eberle.jewelry.shop.backend.entities;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "page_views")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageView {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "page_path", nullable = false, length = 500)
    private String pagePath;
    
    @Column(name = "page_title", length = 255)
    private String pageTitle;
    
    @Column(name = "session_id", length = 255)
    private String sessionId;
    
    @Column(name = "ip_address")
    private InetAddress ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "country_code", length = 2)
    private String countryCode;
    
    @Column(name = "referrer", columnDefinition = "TEXT")
    private String referrer;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}





