package andreas.kafkis.eberle.jewelry.shop.backend.entities;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_cents", nullable = false)
    private Long priceCents;

    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency = "CHF";

    @Column(length = 100)
    private String material;

    @Column(length = 100)
    private String gemstone;

    @Column(name = "weight_grams", precision = 10, scale = 3)
    private java.math.BigDecimal weightGrams;

    @Column(name = "ring_size", length = 20)
    private String ringSize;

    @Column(name = "chain_length", length = 20)
    private String chainLength;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "finish", length = 50)
    private String finish;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    // Special offer fields
    @Column(name = "special_offer", nullable = false)
    private boolean specialOffer = false;

    @Column(name = "special_offer_price_cents")
    private Long specialOfferPriceCents;

    @Column(name = "special_offer_description", columnDefinition = "TEXT")
    private String specialOfferDescription;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "show_in_featured", nullable = false)
    private Boolean showInFeatured = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "product_tags",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images;

    // Custom getter to convert priceCents to price
    public BigDecimal getPrice() {
        return priceCents != null ? new BigDecimal(priceCents).divide(new BigDecimal(100)) : BigDecimal.ZERO;
    }

    // Custom getter to convert specialOfferPriceCents to specialOfferPrice
    public BigDecimal getSpecialOfferPrice() {
        return specialOfferPriceCents != null ? new BigDecimal(specialOfferPriceCents).divide(new BigDecimal(100)) : null;
    }
}

