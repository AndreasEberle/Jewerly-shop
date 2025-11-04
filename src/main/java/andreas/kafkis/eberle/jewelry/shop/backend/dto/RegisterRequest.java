package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
             message = "Password must contain at least one uppercase letter, one lowercase letter, and one number")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]*$", message = "Phone number format is invalid")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phone;
    
    @Size(max = 10, message = "Phone country code must not exceed 10 characters")
    private String countryCode;
    
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;
    
    private String dateOfBirth;
    
    @Pattern(regexp = "^(male|female|other|prefer-not-to-say)$", message = "Gender must be one of: male, female, other, prefer-not-to-say")
    private String gender;
    
    private Boolean newsletterSubscribed;
    
    private AddressRequest address;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddressRequest {
        @Size(max = 255, message = "Street address must not exceed 255 characters")
        private String street;
        
        @Size(max = 100, message = "City must not exceed 100 characters")
        private String city;
        
        @Size(max = 100, message = "State must not exceed 100 characters")
        private String state;
        
        @Size(max = 30, message = "ZIP code must not exceed 30 characters")
        private String zipCode;
        
        @Size(max = 100, message = "Country must not exceed 100 characters")
        private String country;
    }
}
