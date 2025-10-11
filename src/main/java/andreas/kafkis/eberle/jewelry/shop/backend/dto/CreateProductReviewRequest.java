package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductReviewRequest {
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;
    
    @NotBlank(message = "Review title is required")
    @Size(max = 255, message = "Review title must not exceed 255 characters")
    private String title;
    
    @NotBlank(message = "Review content is required")
    @Size(max = 2000, message = "Review content must not exceed 2000 characters")
    private String content;
    
    private Boolean isVerifiedPurchase = false;
}
