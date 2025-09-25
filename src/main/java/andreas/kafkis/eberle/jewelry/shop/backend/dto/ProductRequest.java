package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import java.math.BigDecimal;
import java.util.Set;

import lombok.Data;

@Data
public class ProductRequest {
    private String sku;
    private String name;
    private String description;
    private BigDecimal priceCents;
    private String currency;
    private String material;
    private String gemstone;
    private Double weightGrams;
    private boolean isActive = true;
    private Set<Integer> categoryIds;
    private Set<Integer> tagIds;
}
