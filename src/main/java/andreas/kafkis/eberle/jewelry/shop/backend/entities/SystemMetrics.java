package andreas.kafkis.eberle.jewelry.shop.backend.entities;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "system_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemMetrics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "metric_name", nullable = false, length = 100)
    private String metricName;
    
    @Column(name = "metric_value", nullable = false, precision = 15, scale = 4)
    private BigDecimal metricValue;
    
    @Column(name = "metric_unit", length = 20)
    private String metricUnit;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb")
    private String tags;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
