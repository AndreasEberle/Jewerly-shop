package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressInfo {
    private String street;
    private String apartment;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
