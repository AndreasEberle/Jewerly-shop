package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidationError {
    private String field;
    private String message;
    private Object rejectedValue;
}
