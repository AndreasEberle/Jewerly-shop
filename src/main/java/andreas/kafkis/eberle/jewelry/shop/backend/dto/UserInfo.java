package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import java.time.OffsetDateTime;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneCountryCode;
    private String phoneNumber;
    private OffsetDateTime dateOfBirth;
    private String gender;
    private String preferredLanguage;
    private Boolean newsletterSubscribed;
    private Boolean marketingEmails;
    private Boolean smsNotifications;
    private Set<String> roles;
    private boolean active;
}